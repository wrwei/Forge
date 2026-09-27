package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Output channel of a controller: receives every emitted {@link OutputEvent}
 * (turn, stop, resume, flag) in order.
 */
public final class Actuator {

    private final List<OutputEvent> emitted = new ArrayList<>();

    /** Emits one output event. */
    public void apply(OutputEvent output) {
        emitted.add(output);
    }

    /** All events emitted since the last {@link #drain()}. */
    public List<OutputEvent> emitted() {
        return List.copyOf(emitted);
    }

    /** Returns and forgets all events emitted since the last drain. */
    public List<OutputEvent> drain() {
        var pending = List.copyOf(emitted);
        emitted.clear();
        return pending;
    }
}
