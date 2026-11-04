package steam_boiler.domain;

/**
 * The five operating modes of the steam-boiler control program (SB-V2). The program is always in
 * exactly one of them and starts in initialization mode, listed first.
 */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
