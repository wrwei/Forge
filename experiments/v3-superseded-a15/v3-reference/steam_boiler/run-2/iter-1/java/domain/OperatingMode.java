package steam_boiler.domain;

/** The value carried by the MODE(m) message: one of the five operating modes, initialization first. */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
