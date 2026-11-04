package steam_boiler.domain;

/**
 * The five operating modes of the steam-boiler control program (SB-V2, SB-DM10). The initial mode,
 * initialization, comes first (SB-Beh1).
 */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
