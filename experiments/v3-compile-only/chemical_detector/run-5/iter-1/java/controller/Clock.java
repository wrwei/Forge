package chemical_detector.controller;

import chemical_detector.annotation.RoboChartType;

/** Mission time source, counting whole time units since construction. */
public final class Clock {

    private static final long NANOS_PER_MILLI = 1_000_000L;

    private final long millisPerUnit;
    private final long origin;

    /** Creates a clock whose time unit lasts the given number of milliseconds (at least 1). */
    public Clock(@RoboChartType("nat") long millisPerUnit) {
        if (millisPerUnit < 1) {
            throw new IllegalArgumentException("millisPerUnit must be at least 1");
        }
        this.millisPerUnit = millisPerUnit;
        this.origin = System.nanoTime();
    }

    /** The number of whole time units elapsed since the clock was created. */
    @RoboChartType("nat")
    public long now() {
        return (System.nanoTime() - origin) / NANOS_PER_MILLI / millisPerUnit;
    }

    /** Blocks the caller for the given number of time units. */
    public void pause(@RoboChartType("nat") long duration) {
        try {
            Thread.sleep(duration * millisPerUnit);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
