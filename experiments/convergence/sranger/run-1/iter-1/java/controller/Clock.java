package sranger.controller;

/**
 * Monotonic time source for the controller's timed Turning → Moving
 * transition. A field assigned from {@code now()} is promoted by the
 * model-extraction pipeline to a RoboChart {@code clock}; the elapsed-time
 * predicate {@code now() - clockResetTime} becomes {@code since(clockResetTime)}.
 *
 * <p>Package-private: only the controller in this package reads the clock.
 */
final class Clock {

    /** Current time in seconds. */
    double now() {
        return System.nanoTime() / 1_000_000_000.0;
    }
}
