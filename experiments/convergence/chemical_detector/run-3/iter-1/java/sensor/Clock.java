package chemdetector.sensor;

/**
 * Monotonic time source for the movement subsystem's stuck-detection
 * window. Recognised by the model extraction as a clock dependency.
 */
public final class Clock {

    private long now;

    public long nowMs() {
        return now;
    }

    public void advance(long ms) {
        this.now = this.now + ms;
    }
}
