package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.event.ModeValue;
import steam_boiler.event.OutputEvent;

/**
 * Receives the program's outgoing messages for the current cycle, which are all emitted
 * together at the end of the cycle, and records the last mode and valve position requested.
 */
public final class SteamBoilerActuator {

    private final List<OutputEvent> outgoing = new ArrayList<>();
    private ModeValue lastMode = ModeValue.MODE_INITIALIZATION;
    private ValvePosition valvePosition = ValvePosition.VALVE_CLOSED;

    /** Issues one outgoing message in the current cycle. */
    public void apply(OutputEvent message) {
        outgoing.add(message);
        if (message instanceof OutputEvent.Mode) {
            OutputEvent.Mode modeMessage = (OutputEvent.Mode) message;
            lastMode = modeMessage.value();
        } else if (message instanceof OutputEvent.Valve) {
            if (valvePosition == ValvePosition.VALVE_CLOSED) {
                valvePosition = ValvePosition.VALVE_OPEN;
            } else {
                valvePosition = ValvePosition.VALVE_CLOSED;
            }
        }
    }

    /** Begins a new cycle with an empty outgoing transmission. */
    public void startCycle() {
        outgoing.clear();
    }

    /** The messages issued so far in the current cycle. */
    public List<OutputEvent> outgoing() {
        return new ArrayList<>(outgoing);
    }

    /** The mode carried by the last MODE message issued. */
    public ModeValue lastMode() {
        return lastMode;
    }

    /** The valve position the VALVE messages issued so far have requested. */
    public ValvePosition valvePosition() {
        return valvePosition;
    }
}
