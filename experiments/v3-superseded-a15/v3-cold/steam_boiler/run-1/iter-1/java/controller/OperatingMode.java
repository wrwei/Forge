package steam_boiler.controller;

/** The five operating modes of the program; initialization first (SB-V2, SB-DM10). */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
