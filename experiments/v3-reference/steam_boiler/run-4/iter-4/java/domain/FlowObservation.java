package steam_boiler.domain;

/**
 * The two observations a pump controller can report (SB-DM4): water flows
 * from its pump into the boiler, or it does not.
 */
public enum FlowObservation {
    WATER_FLOWS,
    NO_WATER_FLOW
}
