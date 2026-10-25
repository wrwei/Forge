package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/** Output port of a controller: collects the events it emits until the system delivers them. */
public final class Actuator {

    private final List<OutputEvent> pending = new ArrayList<>();

    /** Emits an output event. */
    public void apply(OutputEvent outputEvent) {
        pending.add(outputEvent);
    }

    /** Returns the events emitted since the last drain, in order, and clears them. */
    public List<OutputEvent> drain() {
        var drained = List.copyOf(pending);
        pending.clear();
        return drained;
    }
}
