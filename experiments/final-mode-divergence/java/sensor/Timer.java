package chemdetector.sensor;

import chemdetector.annotation.Clock;

/**
 * Time source for stuck-detection (CD-MV-Clock1). The movement
 * controller records {@code nowMs()} into its elapsed-time field; the
 * M2M (recognising the {@code @Clock} marker) promotes that field into a
 * RoboChart {@code clock}.
 */
@Clock
public final class Timer {

    public long nowMs() {
        return 0L;
    }
}
