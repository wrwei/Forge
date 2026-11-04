package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.event.OutputEvent;

/**
 * Collects the messages the controller issues during one cycle, so that the
 * transmission stage can emit them together.
 */
public final class BoilerActuator {

    private final List<OutputEvent> issued = new ArrayList<>();

    /** Issues one outgoing message. */
    public void apply(OutputEvent message) {
        issued.add(message);
    }

    /** Returns the messages issued since the last call, and forgets them. */
    public List<OutputEvent> takeIssued() {
        List<OutputEvent> taken = new ArrayList<>(issued);
        issued.clear();
        return taken;
    }
}
