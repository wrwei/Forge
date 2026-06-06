package chemdetector.sensor;

/**
 * Wall-clock time source for stuck detection.
 */
public final class Clock {

    public long nowMs() {
        return System.currentTimeMillis();
    }
}
