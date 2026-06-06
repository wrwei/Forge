package chemdetector.data;

import chemdetector.annotation.RoboChartType;

/**
 * CD-DM5 — Totally-ordered intensity reading.
 * The only required ordering predicate is goreq (CD-Fn4).
 */
public record Intensity(@RoboChartType("real") double value) {

    /**
     * CD-Fn4 — Returns true iff a is at least as large as b.
     */
    public static boolean goreq(Intensity a, Intensity b) {
        return a.value >= b.value;
    }
}
