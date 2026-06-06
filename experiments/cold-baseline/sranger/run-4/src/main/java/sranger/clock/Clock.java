package sranger.clock;

/**
 * Wall-clock dependency for the SRanger controller. The class is named
 * {@code Clock} so the M2M transformation recognises it as the RoboChart
 * clock dependency by convention.
 */
public final class Clock {

    /**
     * Returns the current time in milliseconds (real-valued). Implementation
     * uses {@link System#nanoTime()} to provide a monotonically increasing
     * reading independent of wall-clock adjustments. The {@code nowMs()}
     * name matches the M2M's time-predicate rewrite, where
     * {@code clock.nowMs() - <clockField> >= CONST} is rewritten into
     * {@code since(<clockField>) >= CONST} in the RoboChart model.
     */
    public double nowMs() {
        return System.nanoTime() / 1.0e6;
    }
}
