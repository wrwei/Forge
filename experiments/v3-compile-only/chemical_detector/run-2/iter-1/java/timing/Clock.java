package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/** Time source of the movement subsystem; one tick is one second. */
public final class Clock {

    private static final long NANOS_PER_TICK = 1000000000L;
    private static final long MILLIS_PER_TICK = 1000L;

    private final long origin;

    /** Starts the clock at tick 0. */
    public Clock() {
        this.origin = System.nanoTime();
    }

    /** Whole ticks elapsed since the clock started. */
    @RoboChartType("nat")
    public long now() {
        return (System.nanoTime() - origin) / NANOS_PER_TICK;
    }

    /** Blocks for the given number of ticks. */
    public void pause(@RoboChartType("nat") long duration) {
        try {
            Thread.sleep(duration * MILLIS_PER_TICK);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
