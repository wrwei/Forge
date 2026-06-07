package chemdetector.actuator;

import chemdetector.event.OutputEvent;

/**
 * Receives the output events emitted by the controllers and tracks the
 * last-issued event for inspection and testing.
 */
public final class Actuator {

    private OutputEvent lastEvent;

    public void apply(OutputEvent signal) {
        this.lastEvent = signal;
    }

    public OutputEvent lastEvent() {
        return lastEvent;
    }
}
