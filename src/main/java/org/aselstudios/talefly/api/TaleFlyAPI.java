package org.aselstudios.talefly.api;

import org.aselstudios.talefly.api.economy.IEconomy;
import org.aselstudios.talefly.api.hook.IClaimHook;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main API interface for TaleFly.
 */
public interface TaleFlyAPI {

    /**
     * Checks if a player currently has flight mode enabled.
     * @param playerUuid The player's UUID.
     * @return true if flying, false otherwise.
     */
    boolean isFlying(UUID playerUuid);

    /**
     * Gets the remaining flight time in seconds.
     * @param playerUuid The player's UUID.
     * @return Remaining seconds.
     */
    int getRemainingFlightTime(UUID playerUuid);

    /**
     * Adds flight time to a player.
     * @param playerUuid The player's UUID.
     * @param seconds Seconds to add (use negative to remove).
     * @return A future with the new total time.
     */
    CompletableFuture<Integer> addFlightTime(UUID playerUuid, int seconds);

    /**
     * Sets the exact flight time for a player.
     * @param playerUuid The player's UUID.
     * @param seconds The specific time in seconds.
     * @return A future with the new total time.
     */
    CompletableFuture<Integer> setFlightTime(UUID playerUuid, int seconds);

    /**
     * Toggles flight mode for a player (handles checks and costs).
     * @param player The player entity.
     * @return A future completing when the toggle is done.
     */
    CompletableFuture<Void> toggleFlight(Player player);

    /**
     * Checks if a player is currently combat-tagged.
     * @param playerUuid The player's UUID.
     * @return true if combat tagged.
     */
    boolean isCombatTagged(UUID playerUuid);

    /**
     * Checks if a player is protected from fall damage.
     * @param playerUuid The player's UUID.
     * @return true if protected.
     */
    boolean isFallDamageProtected(UUID playerUuid);

    /**
     * Registers a custom claim hook integration.
     * @param hook The hook implementation.
     */
    void registerClaimHook(IClaimHook hook);

    /**
     * Registers a custom economy provider.
     * Use this if you have a custom economy plugin like "MehmetEcon".
     * This will OVERRIDE any auto-detected economy (Vault, EcoAPI etc.).
     * @param economyProvider The implementation of IEconomy.
     */
    void registerEconomyProvider(IEconomy economyProvider);
}