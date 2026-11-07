package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Receives the events a subsystem emits (turn, stop, resume, flag) and records
 * them in order, for delivery to the other subsystem or the Vehicle.
 */
public final class Actuator {

    private final List<OutputEvent> issued = new ArrayList<>();

    /** Emits one event. */
    public void apply(OutputEvent signal) {
        issued.add(signal);
    }

    /** The events emitted so far, oldest first. */
    public List<OutputEvent> issued() {
        return List.copyOf(issued);
    }
}
