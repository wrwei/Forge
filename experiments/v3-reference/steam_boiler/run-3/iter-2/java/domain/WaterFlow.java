package steam_boiler.domain;

/**
 * The two observations a pump controller can report: water does not flow, or water
 * flows from the pump into the boiler (SB-DM4).
 */
public enum WaterFlow {
    NO_FLOW,
    FLOWING
}
