package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/** Output port of a controller: collects the events it emits, in order. */
public final class EventActuator {

    private final List<OutputEvent> emitted = new ArrayList<>();

    /** Emits one output event. */
    public void apply(OutputEvent outputEvent) {
        emitted.add(outputEvent);
    }

    /** Every event emitted and not yet drained. */
    public List<OutputEvent> emitted() {
        return List.copyOf(emitted);
    }

    /** Returns the pending events and clears them. */
    public List<OutputEvent> drain() {
        var pending = List.copyOf(emitted);
        emitted.clear();
        return pending;
    }
}
