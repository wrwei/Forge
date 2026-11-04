package steam_boiler.controller;

/**
 * The five operating modes of the program; it starts in initialization.
 */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
