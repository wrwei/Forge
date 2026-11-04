package steam_boiler.domain;

/**
 * The operating mode as carried by the MODE(m) message: exactly five alternatives.
 */
public enum ModeIndicator {
    INITIALIZATION_MODE,
    NORMAL_MODE,
    DEGRADED_MODE,
    RESCUE_MODE,
    EMERGENCY_STOP_MODE
}
