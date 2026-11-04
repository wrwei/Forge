package steam_boiler.domain;

/**
 * The value carried by the MODE(m) message: one of the five operating modes, initialization
 * first. The literals carry a MODE_ prefix so that they do not share a name with the controller's
 * states.
 */
public enum OperatingMode {
    MODE_INITIALIZATION,
    MODE_NORMAL,
    MODE_DEGRADED,
    MODE_RESCUE,
    MODE_EMERGENCY_STOP
}
