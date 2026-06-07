package sranger.time;

/**
 * Monotonic time source for the controller's timed transitions.
 */
public final class Clock {

    /** Current monotonic time in seconds. */
    public double now() {
        return System.nanoTime() / 1.0e9;
    }
}
