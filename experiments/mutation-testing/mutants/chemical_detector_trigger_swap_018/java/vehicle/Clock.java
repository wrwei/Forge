package chemdetector.vehicle;

/**
 * Monotonic clock used by the movement subsystem's stuck-detection rule
 * (CD-MV-Clock1). Time is measured in arbitrary "ticks"; the application
 * advances the clock at every {@code step()} call.
 */
public final class Clock {

    private long nowTicks;

    /** Current absolute time in ticks. */
    public long nowMs() {
        return nowTicks;
    }

    /** Advance the clock by one tick (called once per controller step). */
    public void tick() {
        this.nowTicks = this.nowTicks + 1;
    }
}
