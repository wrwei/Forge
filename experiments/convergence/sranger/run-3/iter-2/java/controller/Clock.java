package sranger.controller;

/**
 * Wall-clock time source used by the controller to time the Turning → Moving
 * transition. The transformation pipeline recognises a {@code Clock}-typed
 * dependency: fields assigned from a Clock method call become RoboChart
 * {@code clock} declarations, and {@code clock.now() - field} time-elapsed
 * predicates translate to RoboChart {@code since(field)} semantics.
 */
public final class Clock {

    /** Current time in seconds. */
    public double now() {
        return System.currentTimeMillis() / 1000.0;
    }
}
