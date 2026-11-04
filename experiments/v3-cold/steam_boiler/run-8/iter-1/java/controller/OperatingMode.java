package steam_boiler.controller;

/**
 * The five operating modes of the program (SB-V2, SB-DM10). The program is
 * always in exactly one of them and starts in {@link #INITIALIZATION}.
 */
public enum OperatingMode {
    /** Start-up: wait for the plant, adjust the level, hand-shake (SB-Beh1 to SB-Beh9). */
    INITIALIZATION,
    /** All units sound; level regulated between N1 and N2 (SB-Beh10). */
    NORMAL,
    /** Some unit other than the level device has failed (SB-Beh15). */
    DEGRADED,
    /** The level device has failed; the level is estimated (SB-Beh19). */
    RESCUE,
    /** Terminal: the program stops (SB-Beh22). */
    EMERGENCY_STOP
}
