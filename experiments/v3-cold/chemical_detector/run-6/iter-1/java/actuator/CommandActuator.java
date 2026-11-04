package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Carries the gas-analysis subsystem's commands (turn, stop, resume) to the
 * movement subsystem (CD-ARCH2, CD-Evt4..6). Records the commands issued
 * until they are taken.
 */
public final class CommandActuator {
    private final List<OutputEvent> issued = new ArrayList<>();

    /** Issues a command. */
    public void apply(OutputEvent command) {
        issued.add(command);
    }

    /** Returns the commands issued since the last call, oldest first. */
    public List<OutputEvent> takeIssued() {
        List<OutputEvent> taken = new ArrayList<>(issued);
        issued.clear();
        return taken;
    }
}
