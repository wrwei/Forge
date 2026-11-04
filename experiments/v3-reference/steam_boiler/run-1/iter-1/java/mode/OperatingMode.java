package steam_boiler.mode;

/**
 * The operating-mode value carried by the MODE(m) message: one of exactly
 * five alternatives.
 */
public enum OperatingMode {
    INITIALISATION_MODE,
    NORMAL_MODE,
    DEGRADED_MODE,
    RESCUE_MODE,
    EMERGENCY_STOP_MODE
}
