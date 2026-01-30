package org.aselstudios.talefly.api;

/**
 * Static access point to retrieve the TaleFlyAPI instance.
 */
public final class TaleFlyProvider {

    private static TaleFlyAPI instance;

    private TaleFlyProvider() {}

    /**
     * Gets the API instance.
     * @return The TaleFlyAPI instance.
     * @throws IllegalStateException if TaleFly is not loaded.
     */
    public static TaleFlyAPI get() {
        if (instance == null) {
            throw new IllegalStateException("TaleFly is not loaded!");
        }
        return instance;
    }

    /**
     * Internal use only. Registers the API implementation.
     * @param api The API implementation.
     */
    public static void register(TaleFlyAPI api) {
        instance = api;
    }

    /**
     * Internal use only. Unregisters the API.
     */
    public static void unregister() {
        instance = null;
    }
}