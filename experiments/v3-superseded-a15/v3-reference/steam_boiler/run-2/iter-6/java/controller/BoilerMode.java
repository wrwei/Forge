package steam_boiler.controller;

/** The controller's operating modes; the program starts in initialization. */
public enum BoilerMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
