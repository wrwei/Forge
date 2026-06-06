package chemdetector.clock;

import chemdetector.annotation.Clock;

/**
 * CD-MV-Clock1: monotonic time source the movement subsystem resets
 * on every Going -> Avoiding and AvoidingAgain -> Avoiding transition.
 * The {@code since} predicate returns the elapsed time since the last
 * reset.
 *
 * <p>The class is annotated {@link Clock} so the ETL recognises it as
 * a RoboChart clock dependency irrespective of the Java class name.
 */
@Clock
public final class StuckClock {

    private long lastResetMs = 0;

    /** Current time in milliseconds. */
    public long nowMs() {
        return System.currentTimeMillis();
    }

    /** Reset the clock. */
    public void reset() {
        this.lastResetMs = nowMs();
    }

    /** Elapsed time, in milliseconds, since the last reset. */
    public long since() {
        return nowMs() - lastResetMs;
    }
}
