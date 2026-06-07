package chemdetector.sensor;

/**
 * Wall-clock source for evasion timing (CD-MV-Clock1).
 */
public final class Clock {

    public long nowMs() {
        return System.currentTimeMillis();
    }
}
