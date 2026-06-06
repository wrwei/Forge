package chemdetector.sensor;

/**
 * Monotonic time source for the movement subsystem's stuck-detection
 * clock (CD-MV-Clock1).
 */
public final class Clock {

    private long ms;

    public void advance(long deltaMs) {
        this.ms = this.ms + deltaMs;
    }

    public long nowMs() {
        return ms;
    }
}
