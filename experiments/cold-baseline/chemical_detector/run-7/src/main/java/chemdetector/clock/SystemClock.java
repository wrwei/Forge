package chemdetector.clock;

import chemdetector.annotation.Clock;

/**
 * Wall-clock used by stuck-detection (CD-MV-Clock1). The controller
 * resets clock-field state via {@code nowMs()} and computes elapsed
 * time as {@code clock.nowMs() - clockField}. The ETL recognises this
 * pattern and emits a RoboChart {@code clock} with {@code since(...)}.
 */
@Clock
public final class SystemClock {

    private long startMs = System.currentTimeMillis();

    public long nowMs() {
        return System.currentTimeMillis() - startMs;
    }
}
