package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Output port of one controller: collects the events it emits until the
 * system drains and routes them.
 */
public final class Actuator {

    private final List<OutputEvent> pending = new ArrayList<>();

    /** Emits {@code output}. */
    public void apply(OutputEvent output) {
        pending.add(output);
    }

    /** Returns the events emitted since the last drain, in order, and clears them. */
    public List<OutputEvent> drain() {
        var emitted = List.copyOf(pending);
        pending.clear();
        return emitted;
    }
}
