package steam_boiler.mode;

/**
 * The mode value carried by the MODE(m) message.
 */
public enum OperatingMode {
    INITIALIZATION_MODE,
    NORMAL_MODE,
    DEGRADED_MODE,
    RESCUE_MODE,
    EMERGENCY_STOP_MODE
}
