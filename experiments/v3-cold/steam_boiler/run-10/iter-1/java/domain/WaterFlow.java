package steam_boiler.domain;

/**
 * The observation a pump controller reports: water circulates from its pump into the boiler, or it
 * does not (SB-DM4).
 */
public enum WaterFlow {
    WATER_NOT_FLOWING,
    WATER_FLOWING
}
