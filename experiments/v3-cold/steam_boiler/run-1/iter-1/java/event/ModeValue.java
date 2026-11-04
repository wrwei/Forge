package steam_boiler.event;

/**
 * The operating-mode value carried by the MODE(m) message.
 */
public enum ModeValue {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
