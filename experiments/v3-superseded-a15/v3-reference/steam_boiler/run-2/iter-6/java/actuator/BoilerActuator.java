package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.event.OutputEvent;

/** Collects the commands the controller issues during a cycle, for the cycle's outgoing transmission. */
public final class BoilerActuator {
    private final List<OutputEvent> issued = new ArrayList<>();

    /** Issues one outgoing message. */
    public void apply(OutputEvent command) {
        issued.add(command);
    }

    /** Returns the messages issued since the last call, in issue order, and forgets them. */
    public List<OutputEvent> takeIssued() {
        List<OutputEvent> batch = new ArrayList<>(issued);
        issued.clear();
        return batch;
    }
}
