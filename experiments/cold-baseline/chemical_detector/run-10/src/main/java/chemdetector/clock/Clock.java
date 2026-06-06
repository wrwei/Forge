package chemdetector.clock;

/**
 * Monotonic clock used by the movement controller for stuck detection
 * (CD-MV-Clock1). Maps to a RoboChart clock; {@code since(T)} returns
 * the elapsed time since the last reset.
 */
public final class Clock {

    private long startMs;

    public Clock() {
        this.startMs = 0L;
    }

    /** Returns the current wall-clock time in milliseconds. */
    public long nowMs() {
        return System.currentTimeMillis();
    }

    /** Returns elapsed milliseconds since the last reset. */
    public long since() {
        return nowMs() - startMs;
    }

    /** Resets the clock to the current time. */
    public void reset() {
        this.startMs = nowMs();
    }
}
