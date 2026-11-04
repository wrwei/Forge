package steam_boiler.event;

/** The operating mode carried by the MODE(m) message: exactly five alternatives. */
public enum ModeValue {
    MODE_INITIALIZATION,
    MODE_NORMAL,
    MODE_DEGRADED,
    MODE_RESCUE,
    MODE_EMERGENCY_STOP
}
