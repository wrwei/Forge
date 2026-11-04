package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.data.ValvePosition;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.OperatingMode;

/**
 * The sending side of the message transmission system: collects the messages of the current
 * cycle's outgoing transmission, which are emitted together at the end of the cycle (SB-V3,
 * SB-DM8), and records the last mode reported and the position the evacuation valve was last
 * ordered to (SB-DM7).
 */
public final class BoilerActuator {

    private final List<OutputEvent> outgoing = new ArrayList<>();
    private OperatingMode lastReportedMode = OperatingMode.INITIALIZATION_MODE;
    private ValvePosition valvePosition = ValvePosition.VALVE_CLOSED;

    /** Adds one message to the outgoing transmission of the current cycle. */
    public void apply(OutputEvent message) {
        outgoing.add(message);
        if (message instanceof OutputEvent.Mode) {
            OutputEvent.Mode modeMessage = (OutputEvent.Mode) message;
            lastReportedMode = modeMessage.modeValue();
        } else if (message instanceof OutputEvent.Valve) {
            if (valvePosition == ValvePosition.VALVE_OPEN) {
                valvePosition = ValvePosition.VALVE_CLOSED;
            } else {
                valvePosition = ValvePosition.VALVE_OPEN;
            }
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

    /** The position the evacuation valve was last ordered to; each VALVE message toggles it. */
    public ValvePosition valvePosition() {
        return valvePosition;
    }
}
