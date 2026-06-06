package chemdetector.domain;

/**
 * Totally-ordered intensity reading (CD-DM5). Comparison is via
 * {@link #goreq(Intensity, Intensity)}.
 */
public record Intensity(double value) {

    /**
     * Intensity-greater-or-equal (CD-Fn4): returns true iff {@code a >= b}.
     */
    public static boolean goreq(Intensity a, Intensity b) {
        return a.value >= b.value;
    }
}
