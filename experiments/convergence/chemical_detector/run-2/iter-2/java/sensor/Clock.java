package chemdetector.sensor;

/**
 * Monotonic time source for the movement subsystem's stuck-detection clock
 * (CD-MV-Clock1). Recognised by the M2M as a clock dependency by class name.
 */
public final class Clock {

    private long now;

    /** Current time in milliseconds. */
    public long nowMs() {
        return now;
    }

    /** Advances the clock; called by the simulation harness. */
    public void advance(long ms) {
        this.now = this.now + ms;
    }
}
