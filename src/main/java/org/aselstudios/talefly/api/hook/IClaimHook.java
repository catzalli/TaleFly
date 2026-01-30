package org.aselstudios.talefly.api.hook;

import java.util.UUID;

public interface IClaimHook {

    boolean isHooked();

    String getName();

    FlightRegion getRegion(UUID playerUuid, String worldName, double x, double z);
}