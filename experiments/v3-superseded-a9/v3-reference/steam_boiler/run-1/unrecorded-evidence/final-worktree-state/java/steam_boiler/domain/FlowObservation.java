package steam_boiler.domain;

/** What a pump controller observes: water flows from its pump into the boiler, or it does not. */
public enum FlowObservation {
    NO_FLOW,
    WATER_FLOWING
}
