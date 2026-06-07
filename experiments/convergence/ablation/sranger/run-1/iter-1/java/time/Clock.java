package sranger.time;

/**
 * Wall-clock time source for the controller's timed Turning → Moving
 * transition. Recognised by the model extraction as the RoboChart
 * clock dependency (class name {@code Clock}).
 */
public final class Clock {

    /** Current time in seconds. */
    public double nowSeconds() {
        return System.nanoTime() / 1.0e9;
    }
}
