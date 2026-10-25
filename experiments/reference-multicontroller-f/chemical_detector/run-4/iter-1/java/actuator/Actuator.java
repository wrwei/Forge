package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Output port collecting the events a controller emits, in emission order.
 */
public final class Actuator {

    private final List<OutputEvent> pending = new ArrayList<>();

    public void apply(OutputEvent outputEvent) {
        pending.add(outputEvent);
    }

    /** Events emitted since the last drain. */
    public List<OutputEvent> pending() {
        return List.copyOf(pending);
    }

    /** Returns and clears the events emitted since the last drain. */
    public List<OutputEvent> drain() {
        var drained = List.copyOf(pending);
        pending.clear();
        return drained;
    }
}
