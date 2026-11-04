package steam_boiler.controller;

/** The five operating modes of the program (SB-V2, SB-DM10); the initial mode first (SB-Beh1). */
public enum OperatingMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
