package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Output channel of a controller: records every emitted event in order so the
 * integration can forward it (to the other controller or to the Vehicle).
 */
public final class Actuator {

    private final List<OutputEvent> emitted = new ArrayList<>();

    public void apply(OutputEvent output) {
        emitted.add(output);
    }

    /** All events emitted so far, oldest first. */
    public List<OutputEvent> emitted() {
        return List.copyOf(emitted);
    }

    /** Returns the events emitted since the last drain and forgets them. */
    public List<OutputEvent> drain() {
        var pending = List.copyOf(emitted);
        emitted.clear();
        return pending;
    }
}
