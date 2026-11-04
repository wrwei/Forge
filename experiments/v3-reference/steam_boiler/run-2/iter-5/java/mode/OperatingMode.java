package steam_boiler.mode;

/**
 * The five operating modes of the steam-boiler control program (SB-V2).
 * The program starts in INITIALIZATION (SB-Beh1).
 */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
