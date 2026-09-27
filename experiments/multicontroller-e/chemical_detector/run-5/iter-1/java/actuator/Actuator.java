package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/** Output port of one controller: records every event the controller emits. */
public final class Actuator {

    private final List<OutputEvent> pending = new ArrayList<>();
    private final List<OutputEvent> history = new ArrayList<>();

    /** Emits one output event. */
    public void apply(OutputEvent output) {
        pending.add(output);
        history.add(output);
    }

    /** Returns and clears the events emitted since the previous drain. */
    public List<OutputEvent> drain() {
        var emitted = List.copyOf(pending);
        pending.clear();
        return emitted;
    }

    /** Every event emitted so far, oldest first. */
    public List<OutputEvent> history() {
        return List.copyOf(history);
    }
}
