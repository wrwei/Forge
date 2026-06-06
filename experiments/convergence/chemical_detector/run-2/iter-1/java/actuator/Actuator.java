package chemdetector.actuator;

import java.util.Optional;

import chemdetector.event.OutputEvent;

/**
 * Receives the events the controllers emit (turn, stop, resume, flag) and
 * tracks the last-issued event for inspection.
 */
public final class Actuator {

    private OutputEvent lastEvent;

    /** Emits an output event. */
    public void apply(OutputEvent e) {
        this.lastEvent = e;
    }

    /** Last event emitted, if any. */
    public Optional<OutputEvent> lastEvent() {
        return Optional.ofNullable(lastEvent);
    }
}
