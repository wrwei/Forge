package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.controller.OperatingMode;
import steam_boiler.event.OutputEvent;

/** Collects the messages the program emits in the transmit stage of a cycle. */
public final class BoilerActuator {
    private final List<OutputEvent> outgoing = new ArrayList<>();
    private OutputEvent lastIssued = new OutputEvent.ModeMessage(OperatingMode.INITIALIZATION);

    /** Queues one outgoing message for this cycle's transmission. */
    public void apply(OutputEvent message) {
        outgoing.add(message);
        lastIssued = message;
    }

    /** The most recent message queued. */
    public OutputEvent lastIssued() {
        return lastIssued;
    }

    /** Returns this cycle's queued messages and clears the queue. */
    public List<OutputEvent> takeOutgoing() {
        List<OutputEvent> batch = new ArrayList<>(outgoing);
        outgoing.clear();
        return batch;
    }
}
