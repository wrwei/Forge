package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.event.OutputEvent;

/**
 * Collects the messages the program issues during a cycle and hands them over as one
 * outgoing transmission, all emitted simultaneously (SB-V3, SB-DM8).
 */
public final class BoilerActuator {

    private final List<OutputEvent> issued = new ArrayList<>();

    /** Issues one message for this cycle's outgoing transmission. */
    public void apply(OutputEvent message) {
        issued.add(message);
    }

    /** Returns the messages issued in this cycle, in issue order, and starts a new cycle. */
    public List<OutputEvent> transmit() {
        List<OutputEvent> transmission = new ArrayList<>(issued);
        issued.clear();
        return transmission;
    }
}
