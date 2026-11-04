package steam_boiler.domain;

/**
 * The five operating modes of the steam-boiler control program (SB-V2, SB-DM10).
 * The program starts in {@link #INITIALIZATION} (SB-Beh1); the MODE(m) message
 * carries one of these values (SB-DM8, SB-Beh24).
 */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
