package steam_boiler.mode;

/**
 * The operating-mode value carried by the MODE(m) message: one alternative per
 * operating mode.
 */
public enum ModeCode {
    MODE_INITIALIZATION,
    MODE_NORMAL,
    MODE_DEGRADED,
    MODE_RESCUE,
    MODE_EMERGENCY_STOP
}
