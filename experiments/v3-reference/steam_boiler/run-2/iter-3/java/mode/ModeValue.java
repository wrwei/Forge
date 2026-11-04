package steam_boiler.mode;

/**
 * The operating-mode value carried by the MODE(m) message (SB-DM10): one
 * alternative per operating mode.
 */
public enum ModeValue {
    INITIALIZATION_MODE,
    NORMAL_MODE,
    DEGRADED_MODE,
    RESCUE_MODE,
    EMERGENCY_STOP_MODE
}
