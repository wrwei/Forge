package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.OperatingMode;

/**
 * The sending side of the message transmission system: collects the messages of the current
 * cycle's outgoing transmission, which are emitted together at the end of the cycle (SB-V3,
 * SB-DM8), and records the last mode reported.
 */
public final class BoilerActuator {

    private final List<OutputEvent> outgoing = new ArrayList<>();
    private OperatingMode lastReportedMode = OperatingMode.INITIALIZATION_MODE;

    /** Adds one message to the outgoing transmission of the current cycle. */
    public void apply(OutputEvent message) {
        outgoing.add(message);
        if (message instanceof OutputEvent.Mode) {
            OutputEvent.Mode modeMessage = (OutputEvent.Mode) message;
            lastReportedMode = modeMessage.modeValue();
        }
    }

    /** Starts a new cycle: the outgoing transmission is emptied. */
    public void beginTransmission() {
        outgoing.clear();
    }

    /** The messages of the current cycle's outgoing transmission, in the order issued. */
    public List<OutputEvent> transmission() {
        return List.copyOf(outgoing);
    }

    /** The mode carried by the last MODE(m) message issued. */
    public OperatingMode lastReportedMode() {
        return lastReportedMode;
    }
}
