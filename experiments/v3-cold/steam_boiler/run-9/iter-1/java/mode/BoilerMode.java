package steam_boiler.mode;

/** The five operating modes of the program (SB-V2, SB-DM10); the initial mode first. */
public enum BoilerMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
