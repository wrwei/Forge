package steam_boiler.domain;

/**
 * What a pump controller observes about the pipe from its pump into the boiler:
 * water flows, or water does not flow.
 */
public enum FlowObservation {
    WATER_NOT_FLOWING,
    WATER_FLOWING
}
