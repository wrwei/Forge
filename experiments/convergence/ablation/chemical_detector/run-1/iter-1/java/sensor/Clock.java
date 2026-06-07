package chemdetector.sensor;

/** Monotonic time source for the evasion-sequence timing. */
public final class Clock {

    public long nowMs() {
        return System.currentTimeMillis();
    }
}
