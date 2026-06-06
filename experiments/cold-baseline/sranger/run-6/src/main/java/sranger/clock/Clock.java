package sranger.clock;

/**
 * Clock dependency referenced by the controller. nowMs() returns the
 * monotonic time in milliseconds. The controller derives elapsed
 * seconds from differences of nowMs() readings.
 */
public final class Clock {

    private long currentMs = 0L;

    public long nowMs() {
        return currentMs;
    }

    public void advance(long deltaMs) {
        this.currentMs = this.currentMs + deltaMs;
    }

    public void setNowMs(long ms) {
        this.currentMs = ms;
    }
}
