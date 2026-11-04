package steam_boiler.mode;

/** The five operating modes of the controller (SB-V2); the program starts in initialization (SB-Beh1). */
public enum BoilerMode {
    INITIALIZATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
