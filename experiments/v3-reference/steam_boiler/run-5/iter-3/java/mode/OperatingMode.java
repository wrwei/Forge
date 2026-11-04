package steam_boiler.mode;

/**
 * The operating-mode value carried by the MODE(m) message (SB-DM10, SB-Beh24). It has one
 * alternative per {@link BoilerMode}; the names differ so that controller states and message
 * values never share a name in the extracted model.
 */
public enum OperatingMode {
    INITIALIZATION_MODE,
    NORMAL_MODE,
    DEGRADED_MODE,
    RESCUE_MODE,
    EMERGENCY_STOP_MODE
}
