package steam_boiler.mode;

/**
 * The five operating modes of the steam-boiler control program. The program is always in exactly
 * one of them and starts in initialization mode, which is therefore listed first. The MODE(m)
 * message carries one of these values.
 */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
