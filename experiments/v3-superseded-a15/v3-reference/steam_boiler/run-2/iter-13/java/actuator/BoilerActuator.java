package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.domain.PumpId;
import steam_boiler.domain.PumpSwitch;
import steam_boiler.domain.ValveSetting;
import steam_boiler.event.OutputEvent;

/** Collects the commands the controller issues during a cycle, for the cycle's outgoing transmission. */
public final class BoilerActuator {
    private final List<OutputEvent> issued = new ArrayList<>();
    private boolean valveOpened = false;

    /**
     * Issues one outgoing message. A {@link OutputEvent.SwitchPumps} order is sent as one
     * OPEN_PUMP(n) or CLOSE_PUMP(n) message per pump; a {@link OutputEvent.DrainValve} order is
     * sent as VALVE when it asks for a change of the valve's state, and otherwise not at all.
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.DrainValve) {
            OutputEvent.DrainValve request = (OutputEvent.DrainValve) command;
            boolean openRequested = request.setting() == ValveSetting.VALVE_OPEN;
            if (openRequested != valveOpened) {
                issued.add(new OutputEvent.Valve());
                valveOpened = openRequested;
            }
        } else if (command instanceof OutputEvent.SwitchPumps) {
            OutputEvent.SwitchPumps order = (OutputEvent.SwitchPumps) command;
            for (PumpId pump : PumpId.values()) {
                if (order.setting() == PumpSwitch.ALL_ON) {
                    issued.add(new OutputEvent.OpenPump(pump));
                } else {
                    issued.add(new OutputEvent.ClosePump(pump));
                }
            }
        } else {
            issued.add(command);
        }
    }

    /** Returns the messages issued since the last call, in issue order, and forgets them. */
    public List<OutputEvent> takeIssued() {
        List<OutputEvent> batch = new ArrayList<>(issued);
        issued.clear();
        return batch;
    }
}
