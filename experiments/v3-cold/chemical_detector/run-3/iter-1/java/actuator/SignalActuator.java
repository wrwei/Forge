package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

import java.util.ArrayList;
import java.util.List;

/** Receives the output events a controller emits, in order. */
public final class SignalActuator {

    private final List<OutputEvent> emitted = new ArrayList<>();

    /** Emits one output event. */
    public void apply(OutputEvent signal) {
        emitted.add(signal);
    }

    /** The events emitted so far, oldest first. */
    public List<OutputEvent> emitted() {
        return List.copyOf(emitted);
    }
}
