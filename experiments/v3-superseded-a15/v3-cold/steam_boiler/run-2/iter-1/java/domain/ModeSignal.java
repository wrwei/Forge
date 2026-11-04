package steam_boiler.domain;

/**
 * The value carried by the MODE(m) message (SB-DM10): one of exactly five alternatives, one per
 * operating mode.
 */
public enum ModeSignal {
    SIGNAL_INITIALIZATION,
    SIGNAL_NORMAL,
    SIGNAL_DEGRADED,
    SIGNAL_RESCUE,
    SIGNAL_EMERGENCY_STOP
}
