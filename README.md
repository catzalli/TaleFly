# TaleFly API

This repository contains the public API for **TaleFly** (Hytale flight plugin).
It is meant to be used by other plugins that want to:

- Read flight state (is the player flying, remaining time, combat tag, fall protection)
- Give / remove flight time
- Toggle flight using TaleFly's own checks (cost, region rules, cooldowns, etc.)
- Provide a custom Claim integration (so TaleFly can ask your claim system what region the player is in)
- Provide a custom Economy integration (if you use an unsupported economy plugin)

The API is provided by the TaleFly plugin at runtime. Your plugin should **depend on TaleFly** and load after it.
---

## Installation

TaleFly API is not hosted on a public Maven repository yet. You must add the jar file manually.

### Step 1: Download
Download `TaleFly-api-0.0.6.jar` from the releases page.

### Step 2: Add to Project
Create a `libs` folder in your project root and place the jar file there.

### Step 3: Gradle Configuration
Add the following to your `build.gradle` dependencies:

```groovy
dependencies {
    compileOnly(fileTree(dir: 'libs', include: ['*.jar']))
}

## Getting the API instance

TaleFly registers the API when it enables. Use `TaleFlyProvider.get()` to access it:

```java
import org.aselstudios.talefly.api.TaleFlyAPI;
import org.aselstudios.talefly.api.TaleFlyProvider;

public class MyPlugin {
    private TaleFlyAPI talefly;

    public void onEnable() {
        try {
            talefly = TaleFlyProvider.get();
        } catch (IllegalStateException e) {
            // TaleFly is not loaded (not installed or not enabled yet)
            // You can disable your integration here.
            talefly = null;
        }
    }
}
```

`TaleFlyProvider.get()` throws `IllegalStateException` if TaleFly is not loaded.

---

## API overview

Main interface: `org.aselstudios.talefly.api.TaleFlyAPI`

### Player state

- `boolean isFlying(UUID playerUuid)`
  - `true` if TaleFly flight mode is currently enabled for that player.

- `int getRemainingFlightTime(UUID playerUuid)`
  - Remaining flight time in seconds (cached). Returns `0` if not known.

- `boolean isCombatTagged(UUID playerUuid)`
  - `true` if the player is currently combat-tagged (TaleFly may block flight during combat).

- `boolean isFallDamageProtected(UUID playerUuid)`
  - `true` if the player is currently protected from fall damage by TaleFly.

### Time management (async)

These methods return `CompletableFuture` because TaleFly persists data (database / storage).

- `CompletableFuture<Integer> addFlightTime(UUID playerUuid, int seconds)`
  - Adds time. Use a negative value to remove time.
  - Completes with the new total time (seconds).

- `CompletableFuture<Integer> setFlightTime(UUID playerUuid, int seconds)`
  - Sets exact time (seconds). Values below 0 are clamped to 0.
  - Completes with the new total time (seconds).

### Flight toggling (async)

- `CompletableFuture<Void> toggleFlight(Player player)`
  - Toggles flight using TaleFly's internal rules (permissions, costs, region rules, combat, etc.).
  - Prefer this instead of manually setting movement states.

### Integrations

- `void registerClaimHook(IClaimHook hook)`
  - Register your claim integration so TaleFly can ask your plugin what region the player is in.

- `void registerEconomyProvider(IEconomy economyProvider)`
  - Register your economy implementation (overrides auto-detected integrations).

---

## Example: Give flight time when a rank is granted

```java
import java.util.UUID;
import org.aselstudios.talefly.api.TaleFlyAPI;

public void onVipGranted(UUID playerUuid, TaleFlyAPI api) {
    api.addFlightTime(playerUuid, 3600).thenAccept(newTotalSeconds -> {
        // Player now has +3600 seconds
    });
}
```

If you need to remove time:
```java
api.addFlightTime(playerUuid, -300);
```

---

## Example: Toggle flight from your own command

```java
import org.aselstudios.talefly.api.TaleFlyAPI;
import com.hypixel.hytale.server.core.entity.entities.Player;

public void onMyFlyCommand(Player player, TaleFlyAPI api) {
    api.toggleFlight(player).thenRun(() -> {
        // Toggle completed (success or fail handling is done by TaleFly internally)
    });
}
```

---

## Claim integration

Package: `org.aselstudios.talefly.api.hook`

Interface:
```java
public interface IClaimHook {
    boolean isHooked();
    String getName();
    FlightRegion getRegion(UUID playerUuid, String worldName, double x, double z);
}
```

Enum:
```java
public enum FlightRegion {
    OWN, ALLY, NEUTRAL, ENEMY, SAFEZONE, WARZONE, WILDERNESS, BLOCKED
}
```

### How TaleFly uses your hook

- TaleFly calls `getRegion(...)` for a player's location.
- Return `WILDERNESS` if the location is not claimed (or your plugin does not manage that world).
- Return `BLOCKED` if you want to hard-block flight in that location.
- For claimed land, return one of: `OWN`, `ALLY`, `NEUTRAL`, `ENEMY`, `SAFEZONE`, `WARZONE`.

TaleFly will apply its own config rules (for example: allow flight in ally claims, enemy claims, wilderness, etc.).

### Example: Custom claim hook

```java
import java.util.UUID;
import org.aselstudios.talefly.api.hook.IClaimHook;
import org.aselstudios.talefly.api.hook.FlightRegion;

public final class MyClaimsHook implements IClaimHook {

    @Override
    public boolean isHooked() {
        // Return true only if your claim system is enabled and ready
        return true;
    }

    @Override
    public String getName() {
        return "MyClaims";
    }

    @Override
    public FlightRegion getRegion(UUID playerUuid, String worldName, double x, double z) {
        // Example pseudo-logic:
        // - if not claimed -> WILDERNESS
        // - if claimed by player -> OWN
        // - if claimed but blocked area -> BLOCKED
        return FlightRegion.WILDERNESS;
    }
}
```

Register it:
```java
import org.aselstudios.talefly.api.TaleFlyAPI;

api.registerClaimHook(new MyClaimsHook());
```

---

## Economy integration

Package: `org.aselstudios.talefly.api.economy`

Interface:
```java
public interface IEconomy {
    boolean isEnabled();
    boolean has(String worldName, UUID playerUuid, BigDecimal amount);
    boolean withdraw(String worldName, UUID playerUuid, BigDecimal amount);
    BigDecimal getBalance(String worldName, UUID playerUuid);
    String format(BigDecimal amount);
}
```

### When to use a custom economy provider

TaleFly can auto-detect some economy plugins. If you use something else, implement `IEconomy` and register it:

```java
import java.math.BigDecimal;
import java.util.UUID;
import org.aselstudios.talefly.api.economy.IEconomy;

public final class MyEconomy implements IEconomy {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean has(String worldName, UUID playerUuid, BigDecimal amount) {
        return getBalance(worldName, playerUuid).compareTo(amount) >= 0;
    }

    @Override
    public boolean withdraw(String worldName, UUID playerUuid, BigDecimal amount) {
        // withdraw from your economy and return true on success
        return true;
    }

    @Override
    public BigDecimal getBalance(String worldName, UUID playerUuid) {
        // return player's balance
        return BigDecimal.ZERO;
    }

    @Override
    public String format(BigDecimal amount) {
        // return formatted currency string
        return amount.toPlainString();
    }
}
```

Register it (this overrides auto-detected economy integrations):
```java
api.registerEconomyProvider(new MyEconomy());
```

---

## Best practices

- Do not block the server thread waiting on a `CompletableFuture`. Use `thenAccept`, `thenRun`, or `exceptionally`.
- Prefer `toggleFlight(Player)` if you want the same behavior players get via `/fly`.
- If you query offline players, `getRemainingFlightTime(UUID)` may return `0` until the data is loaded. Use `setFlightTime` / `addFlightTime` and read the value from the returned future if you need a guaranteed value.

PS. Parts of this documentation were generated with AI assistance. Please report any inaccuracies.
