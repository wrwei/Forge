package steam_boiler.mode;

/** The operating mode carried by the MODE(m) message: exactly five alternatives. */
public enum OperatingMode {
    INITIALIZATION_MODE,
    NORMAL_MODE,
    DEGRADED_MODE,
    RESCUE_MODE,
    EMERGENCY_STOP_MODE
}
