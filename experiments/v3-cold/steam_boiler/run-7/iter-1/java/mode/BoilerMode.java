package steam_boiler.mode;

/**
 * The operating modes of the controller; the initial mode first.
 */
public enum BoilerMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
