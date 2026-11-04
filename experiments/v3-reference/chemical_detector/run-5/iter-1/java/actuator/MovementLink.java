package chemical_detector.actuator;

import chemical_detector.event.OutputEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The link from the gas-analysis subsystem to the movement subsystem: carries the
 * turn, stop and resume events and records every command issued.
 */
public final class MovementLink {

    private final List<OutputEvent> issued = new ArrayList<>();

    /**
     * Sends a command event to the movement subsystem.
     *
     * @param command the turn, stop or resume event
     */
    public void apply(OutputEvent command) {
        issued.add(command);
    }

    /**
     * The commands issued so far, oldest first.
     *
     * @return an unmodifiable copy of the issued commands
     */
    public List<OutputEvent> issuedCommands() {
        return List.copyOf(issued);
    }
}
