package steam_boiler.mode;

/**
 * The five operating modes of the steam-boiler control program; the program
 * starts in {@link #INITIALIZATION}.
 */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
