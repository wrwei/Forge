package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects the events the controllers emit until the system delivers them.
 */
public final class Actuator {

    private final List<OutputEvent> emitted = new ArrayList<>();

    /** Emits an output event. */
    public void apply(OutputEvent output) {
        emitted.add(output);
    }

    /** Returns and clears the events emitted since the last drain. */
    public List<OutputEvent> drain() {
        var pending = List.copyOf(emitted);
        emitted.clear();
        return pending;
    }
}
