package chemdetector.data;

import chemdetector.annotation.RoboChartType;

/**
 * One gas-sensor reading: the chemical identity {@code c} (an opaque
 * identifier compared by equality only) and the measured intensity
 * {@code i} for that chemical.
 */
public record GasSensor(@RoboChartType("nat") int c,
                        @RoboChartType("real") double i) {
}
