package steam_boiler.controller;

/**
 * The five operating modes of the program (SB-V2); the program starts in
 * initialization mode (SB-Beh1).
 */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
