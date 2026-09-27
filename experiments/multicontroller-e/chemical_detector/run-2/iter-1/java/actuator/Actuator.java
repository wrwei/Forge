package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/** Output port of a controller: collects the events it emits until the system collects them. */
public final class Actuator {

    private final List<OutputEvent> emitted = new ArrayList<>();

    /** Emits one output event. */
    public void apply(OutputEvent out) {
        emitted.add(out);
    }

    /** Returns the events emitted since the last call, oldest first, and forgets them. */
    public List<OutputEvent> drain() {
        var batch = List.copyOf(emitted);
        emitted.clear();
        return batch;
    }
}
