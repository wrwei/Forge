package steam_boiler.monitor;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.domain.OperatingMode;
import steam_boiler.event.OutputEvent;

/**
 * Water-level regulation within the operating mode the controller has decided for the
 * cycle. In initialization it drains or fills the boiler until the level lies between
 * N1 and N2 and then announces PROGRAM_READY (SB-Beh4, SB-Beh5, SB-Beh7); in normal,
 * degraded and rescue mode it switches the pumps so that the level expected at the end
 * of the coming cycle stays between N1 and N2 (SB-Beh10, SB-Beh15, SB-Beh19). In
 * emergency stop it issues nothing (SB-Beh22).
 */
public final class LevelRegulator {

    @RoboChartType("nat") private static final int FILLING_PUMP = 1;

    private final PlantMonitor monitor;

    /** Regulates on the analysis of the given monitor. */
    public LevelRegulator(PlantMonitor monitor) {
        this.monitor = monitor;
    }

    /** The regulation orders of this cycle, for the mode the controller has just set. */
    public List<OutputEvent> orders(OperatingMode mode) {
        List<OutputEvent> orders = new ArrayList<>();
        if (mode == OperatingMode.INITIALIZATION) {
            initialisationOrders(orders);
        } else if (mode == OperatingMode.NORMAL || mode == OperatingMode.DEGRADED || mode == OperatingMode.RESCUE) {
            pumpOrders(orders);
        }
        return orders;
    }

    private void initialisationOrders(List<OutputEvent> orders) {
        boolean levelHigh = monitor.levelAboveNormal();
        boolean levelLow = monitor.levelBelowNormal();
        boolean valveOpen = monitor.evacuationValveOpen();
        if (!monitor.steamBoilerWaitingReceived()) {
            // SB-Beh2: nothing happens before STEAM_BOILER_WAITING.
            return;
        }
        if (monitor.programReadySent()) {
            // SB-Beh7: repeat PROGRAM_READY until PHYSICAL_UNITS_READY.
            orders.add(new OutputEvent.ProgramReady());
        } else if (levelHigh && !valveOpen) {
            // SB-Beh4: level above N2, open the evacuation valve and stop filling.
            orders.add(new OutputEvent.Valve());
            orders.add(new OutputEvent.ClosePump(FILLING_PUMP));
        } else if (levelLow && valveOpen) {
            // SB-Beh5: level below N1 after draining, close the valve and fill.
            orders.add(new OutputEvent.Valve());
            orders.add(new OutputEvent.OpenPump(FILLING_PUMP));
        } else if (levelLow) {
            // SB-Beh5: level below N1, activate a pump.
            orders.add(new OutputEvent.OpenPump(FILLING_PUMP));
        } else if (!levelHigh) {
            // SB-Beh7: level between N1 and N2: stop draining or filling, announce PROGRAM_READY.
            if (valveOpen) {
                orders.add(new OutputEvent.Valve());
            }
            orders.add(new OutputEvent.ClosePump(FILLING_PUMP));
            orders.add(new OutputEvent.ProgramReady());
        }
        // Level above N2 with the valve open: draining continues (SB-Beh4).
    }

    private void pumpOrders(List<OutputEvent> orders) {
        if (monitor.predictedLevelAboveNormal()) {
            for (int n = 1; n <= BoilerConstants.PUMP_COUNT; n++) {
                orders.add(new OutputEvent.ClosePump(n));
            }
        } else if (monitor.predictedLevelBelowNormal()) {
            for (int n = 1; n <= BoilerConstants.PUMP_COUNT; n++) {
                orders.add(new OutputEvent.OpenPump(n));
            }
        }
    }
}
