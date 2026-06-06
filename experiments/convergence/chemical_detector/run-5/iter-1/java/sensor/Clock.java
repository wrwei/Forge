package chemdetector.sensor;

/**
 * Time source for the movement subsystem's stuck-detection clock.
 */
public class Clock {

    public long nowMs() {
        return System.currentTimeMillis();
    }
}
