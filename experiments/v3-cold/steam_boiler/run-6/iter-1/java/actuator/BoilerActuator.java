package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.BoilerConstants;
import steam_boiler.domain.PumpId;
import steam_boiler.event.OutputEvent;

/**
 * Receives the program's outgoing messages for the current cycle, emits them
 * together at the end of the cycle, and records what was last commanded: the
 * state of the evacuation valve, whether PROGRAM_READY has been sent, and the
 * pump orders of the last transmission.
 */
public final class BoilerActuator {

    private final List<OutputEvent> outgoing = new ArrayList<>();
    private final boolean[] openOrdered = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] closeOrdered = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] lastOpenOrdered = new boolean[BoilerConstants.PUMP_COUNT];
    private final boolean[] lastCloseOrdered = new boolean[BoilerConstants.PUMP_COUNT];
    private boolean valveOpen = false;
    private boolean programReadySent = false;

    /**
     * Adds a message to the current cycle's outgoing transmission.
     *
     * @param message the message to send
     */
    public void apply(OutputEvent message) {
        outgoing.add(message);
        if (message instanceof OutputEvent.Valve) {
            valveOpen = !valveOpen;
        } else if (message instanceof OutputEvent.ProgramReady) {
            programReadySent = true;
        } else if (message instanceof OutputEvent.OpenPump) {
            OutputEvent.OpenPump order = (OutputEvent.OpenPump) message;
            openOrdered[order.pump().ordinal()] = true;
            closeOrdered[order.pump().ordinal()] = false;
        } else if (message instanceof OutputEvent.ClosePump) {
            OutputEvent.ClosePump order = (OutputEvent.ClosePump) message;
            closeOrdered[order.pump().ordinal()] = true;
            openOrdered[order.pump().ordinal()] = false;
        }
    }

    /**
     * Emits the current cycle's messages, all at once, and starts the next
     * cycle's transmission.
     *
     * @return the messages of the cycle, in the order they were issued
     */
    public List<OutputEvent> transmit() {
        List<OutputEvent> transmission = new ArrayList<>(outgoing);
        outgoing.clear();
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            lastOpenOrdered[i] = openOrdered[i];
            lastCloseOrdered[i] = closeOrdered[i];
            openOrdered[i] = false;
            closeOrdered[i] = false;
        }
        return transmission;
    }

    /**
     * Whether the evacuation valve is open, following the VALVE messages sent
     * so far (each VALVE message toggles it).
     *
     * @return true when the valve has been opened and not closed again
     */
    public boolean valveOpen() {
        return valveOpen;
    }

    /**
     * Whether PROGRAM_READY has been sent in some earlier message.
     *
     * @return true once PROGRAM_READY has been issued
     */
    public boolean programReadySent() {
        return programReadySent;
    }

    /**
     * Whether the last transmission ordered the given pump on.
     *
     * @param pump the pump
     * @return true when OPEN_PUMP(pump) was in the last transmission
     */
    public boolean openOrderedLast(PumpId pump) {
        return lastOpenOrdered[pump.ordinal()];
    }

    /**
     * Whether the last transmission ordered the given pump off.
     *
     * @param pump the pump
     * @return true when CLOSE_PUMP(pump) was in the last transmission
     */
    public boolean closeOrderedLast(PumpId pump) {
        return lastCloseOrdered[pump.ordinal()];
    }
}
