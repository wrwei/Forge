package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Link from the gas-analysis subsystem to the movement subsystem. Holds the
 * turn / stop / resume commands issued until the system delivers them.
 */
public final class CommandChannel {

    private final List<OutputEvent> issued = new ArrayList<>();

    /** Issues a command. */
    public void apply(OutputEvent command) {
        issued.add(command);
    }

    /** Returns the commands issued since the last call, in order, and forgets them. */
    public List<OutputEvent> drain() {
        var commands = List.copyOf(issued);
        issued.clear();
        return commands;
    }
}
