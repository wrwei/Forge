package steam_boiler.event;

/**
 * The operating mode as carried by the MODE(m) message (SB-DM10): exactly
 * five alternatives.
 */
public enum ModeValue {
    INITIALIZATION_MODE,
    NORMAL_MODE,
    DEGRADED_MODE,
    RESCUE_MODE,
    EMERGENCY_STOP_MODE
}
