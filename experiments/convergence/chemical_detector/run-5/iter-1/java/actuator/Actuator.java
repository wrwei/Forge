package chemdetector.actuator;

import chemdetector.event.OutputEvent;

/**
 * Output surface: receives events emitted by the controllers.
 */
public final class Actuator {

    private OutputEvent lastEvent;

    public void apply(OutputEvent value) {
        this.lastEvent = value;
    }

    public OutputEvent lastEvent() {
        return lastEvent;
    }
}
