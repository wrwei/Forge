package chemdetector.function;

import chemdetector.data.Intensity;

/**
 * Pure ordering predicate on {@link Intensity} (CD-Fn4).
 */
public final class IntensityFunctions {

    /**
     * CD-Fn4: {@code true} iff {@code a >= b}.
     */
    public static boolean goreq(Intensity a, Intensity b) {
        return a.value() >= b.value();
    }

    private IntensityFunctions() {
    }
}
