package org.aselstudios.talefly.api.economy;

import java.math.BigDecimal;
import java.util.UUID;

public interface IEconomy {
    boolean isEnabled();
    boolean has(String worldName, UUID playerUuid, BigDecimal amount);
    boolean withdraw(String worldName, UUID playerUuid, BigDecimal amount);
    BigDecimal getBalance(String worldName, UUID playerUuid);
    String format(BigDecimal amount);
}