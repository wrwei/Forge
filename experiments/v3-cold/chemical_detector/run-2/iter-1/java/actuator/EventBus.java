package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

/**
 * Receives the events the controllers send: turn, stop and resume from the
 * gas-analysis subsystem (delivered to the movement subsystem) and flag from
 * the movement subsystem (delivered to the Vehicle). Records the last one.
 */
public final class EventBus {
    private OutputEvent lastEvent = null;

    /**
     * Sends an event.
     *
     * @param event the event sent
     */
    public void apply(OutputEvent event) {
        this.lastEvent = event;
    }

    /**
     * The last event sent, or null if none has been sent.
     *
     * @return the last event
     */
    public OutputEvent lastEvent() {
        return lastEvent;
    }
}
