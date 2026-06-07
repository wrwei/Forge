package chemdetector.time;

/**
 * Wall-clock time source for evasion timing (CD-MV-Clock1). Recognised
 * by the model extraction as a clock dependency.
 */
public class Clock {

    public long nowMs() {
        return System.currentTimeMillis();
    }
}
