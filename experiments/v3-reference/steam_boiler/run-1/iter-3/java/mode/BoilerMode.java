package steam_boiler.mode;

/**
 * The five operating modes of the program; it is always in exactly one of
 * them and starts in initialisation mode.
 */
public enum BoilerMode {
    INITIALISATION,
    NORMAL,
    DEGRADED,
    RESCUE,
    EMERGENCY_STOP
}
