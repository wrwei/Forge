package chemdetector.sensor;

/**
 * Monotonic time source for the movement subsystem's stuck-detection
 * timing. Advanced externally by the platform.
 */
public final class Clock {

    private long ms;

    /** Current time in milliseconds since system start. */
    public long nowMs() {
        return ms;
    }

    /** Advances the clock by {@code delta} milliseconds. */
    public void tick(long delta) {
        this.ms = this.ms + delta;
    }
}
