package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/** Output port of a controller: collects the events it emits until they are delivered. */
public final class Actuator {

    private final List<OutputEvent> emitted = new ArrayList<>();

    /** Emits one output event. */
    public void apply(OutputEvent outputEvent) {
        emitted.add(outputEvent);
    }

    /** Returns the events emitted since the last drain, oldest first, and clears them. */
    public List<OutputEvent> drain() {
        var drained = List.copyOf(emitted);
        emitted.clear();
        return drained;
    }
}
