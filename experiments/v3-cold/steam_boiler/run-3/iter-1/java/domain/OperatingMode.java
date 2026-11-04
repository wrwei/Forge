package steam_boiler.domain;

/**
 * The five operating modes of the steam-boiler control program; the program is
 * always in exactly one of them and starts in initialization mode. The MODE(m)
 * message carries one of these values.
 */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
