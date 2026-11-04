package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Output port of the gas-analysis subsystem (CD-Evt4..6). It records the
 * turn, stop and resume events issued; the integrating platform drains them
 * and delivers each to the movement subsystem as the input event of the same
 * name.
 */
public final class GasAnalysisActuator {
    private final List<OutputEvent> pending = new ArrayList<>();

    /** Emits one event towards the movement subsystem. */
    public void apply(OutputEvent output) {
        pending.add(output);
    }

    /** The events emitted since the previous drain, oldest first. */
    public List<OutputEvent> drain() {
        List<OutputEvent> issued = new ArrayList<>(pending);
        pending.clear();
        return issued;
    }
}
