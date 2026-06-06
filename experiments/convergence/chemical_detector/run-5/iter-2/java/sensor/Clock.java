package chemdetector.sensor;

/**
 * Time source for stuck detection (CD-MV-Clock1).
 */
public final class Clock {

    public long nowMs() {
        return System.currentTimeMillis();
    }
}
