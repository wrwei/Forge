package chemdetector.sensor;

/** Millisecond time source; controller fields assigned from nowMs() become RoboChart clocks. */
public final class Clock {

    public long nowMs() {
        return System.currentTimeMillis();
    }
}
