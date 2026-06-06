package chemdetector.datatype;

import chemdetector.annotation.RoboChartType;

/**
 * CD-DM5. Intensity: totally-ordered measurement of sensor reading strength.
 * Comparison via the {@code goreq} static helper (see CD-Fn4).
 */
public record Intensity(@RoboChartType("real") double value) {

    /**
     * CD-Fn4. goreq: returns true iff a >= b.
     */
    public static boolean goreq(Intensity a, Intensity b) {
        return a.value >= b.value;
    }
}
