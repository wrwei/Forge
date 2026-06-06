package sranger.time;

import sranger.annotation.RoboChartType;

/**
 * Provides the current wall-clock time used for the timed Turning → Moving
 * transition. A controller field assigned from {@code clock.nowMs()} is
 * promoted by the model extraction to a RoboChart {@code clock}, and an elapsed
 * predicate of the form {@code clock.nowMs() - field >= CONST} becomes
 * {@code since(field) >= CONST}.
 */
public final class Clock {

    /** The current time, in seconds. */
    @RoboChartType("real")
    public double nowMs() {
        return System.nanoTime() / 1_000_000_000.0;
    }
}
