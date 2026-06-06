package chemdetector.actuator;

import java.util.Optional;

import chemdetector.event.OutputEvent;

/**
 * Receives output events emitted by the controllers.
 */
public final class Actuator {

    private OutputEvent lastEvent;

    public void apply(OutputEvent signal) {
        this.lastEvent = signal;
    }

    public Optional<OutputEvent> lastEvent() {
        return Optional.ofNullable(lastEvent);
    }
}
