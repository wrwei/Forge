package steam_boiler.controller;

/** The five operating modes of the steam-boiler program; the initial mode first. */
public enum BoilerMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
