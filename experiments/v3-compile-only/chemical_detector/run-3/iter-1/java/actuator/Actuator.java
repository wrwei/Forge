package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

/** Output port of a controller: carries the events it emits. */
public final class Actuator {
    private OutputEvent lastEvent = null;

    /** Emits an output event. */
    public void apply(OutputEvent signal) {
        this.lastEvent = signal;
    }

    /** The last event emitted, or null if none yet. */
    public OutputEvent lastEvent() {
        return lastEvent;
    }
}
