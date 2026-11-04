package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.controller.BoilerMode;
import steam_boiler.event.OutputEvent;

/**
 * Receives the controller's outgoing messages for the current cycle. All messages of
 * a cycle are emitted together when the program drains the outbox; the actuator also
 * records the last mode it was asked to broadcast.
 */
public final class BoilerActuator {
    private final List<OutputEvent> outbox = new ArrayList<>();
    private BoilerMode lastMode = BoilerMode.INITIALIZATION;

    /** Queues one outgoing message for this cycle's transmission. */
    public void apply(OutputEvent message) {
        if (message instanceof OutputEvent.Mode) {
            OutputEvent.Mode mode = (OutputEvent.Mode) message;
            lastMode = mode.value();
        }
        outbox.add(message);
    }

    /** Returns the messages queued this cycle, in order, and empties the outbox. */
    public List<OutputEvent> drain() {
        List<OutputEvent> messages = new ArrayList<>(outbox);
        outbox.clear();
        return messages;
    }

    /** The mode most recently broadcast. */
    public BoilerMode lastMode() {
        return lastMode;
    }
}
