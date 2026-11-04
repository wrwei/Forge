package steam_boiler.controller;

/** The program's operating modes; it starts in initialization. */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
