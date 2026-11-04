package steam_boiler.mode;

/**
 * The states of the steam-boiler controller, the initial state first.
 *
 * <p>The four INITIALIZATION states together form the initialization operating mode
 * (all of them broadcast {@link OperatingMode#INITIALIZATION_MODE}); the other four
 * states are the normal, degraded, rescue and emergency stop operating modes.
 */
public enum BoilerMode {
    /** Initialization: waiting for STEAM_BOILER_WAITING. */
    INITIALIZATION,
    /** Initialization: the level is below N1 and the pumps are filling the boiler. */
    INITIALIZATION_FILLING,
    /** Initialization: the level is above N2 and the evacuation valve is open. */
    INITIALIZATION_DRAINING,
    /** Initialization: PROGRAM_READY announced, waiting for PHYSICAL_UNITS_READY. */
    INITIALIZATION_READY,
    /** Normal mode: every unit sound, the level is regulated between N1 and N2. */
    NORMAL,
    /** Degraded mode: some unit other than the level device has failed. */
    DEGRADED,
    /** Rescue mode: the level device has failed, the level is estimated. */
    RESCUE,
    /** Emergency stop mode: terminal. */
    EMERGENCY_STOP
}
