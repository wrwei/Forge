package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Emits a controller's output events and records them in order.
 */
public final class Actuator {

    private final List<OutputEvent> emitted = new ArrayList<>();

    /** Emits {@code out}. */
    public void apply(OutputEvent out) {
        emitted.add(out);
    }

    /** Events emitted so far, oldest first. */
    public List<OutputEvent> emitted() {
        return List.copyOf(emitted);
    }
}
