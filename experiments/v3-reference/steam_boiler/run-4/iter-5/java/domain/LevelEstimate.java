package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The computed water level (SB-V8), kept as a lower and an upper bound on the
 * true level, in litres. While the level device works both bounds equal the
 * last reading.
 *
 * @param lowerBound the least quantity of water the boiler can contain
 * @param upperBound the greatest quantity of water the boiler can contain
 */
public record LevelEstimate(@RoboChartType("real") double lowerBound, @RoboChartType("real") double upperBound) {
}
