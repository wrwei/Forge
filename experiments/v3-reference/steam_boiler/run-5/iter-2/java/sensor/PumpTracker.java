package steam_boiler.sensor;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.data.PumpPosition;
import steam_boiler.data.WaterFlow;

/**
 * What the program knows about one pump and its controller across cycles: the reported pump
 * state and controller observation in this and the previous transmission, and the last order
 * that changed the pump's commanded position, with the number of transmissions since it was
 * issued. Supports pump failure detection (SB-FR1), pump-controller failure detection
 * (SB-FR2) and the start-up latency (SB-Beh25).
 */
final class PumpTracker {

    /** Transmissions after an order beyond which the pump and its controller are settled. */
    private static final int SETTLED_AFTER = 3;

    private PumpPosition position = PumpPosition.PUMP_OFF;
    private PumpPosition previousPosition = PumpPosition.PUMP_OFF;
    private WaterFlow flow = WaterFlow.NOT_FLOWING;
    private WaterFlow previousFlow = WaterFlow.NOT_FLOWING;
    private boolean baseline = false;
    private boolean commanded = false;
    private PumpPosition commandedPosition = PumpPosition.PUMP_OFF;
    @RoboChartType("nat") private int transmissionsSinceOrder = 0;

    void readPosition(PumpPosition reported) {
        position = reported;
    }

    void readFlow(WaterFlow reported) {
        flow = reported;
    }

    /** A new transmission has arrived: one more transmission since the last order. */
    void advanceCycle() {
        if (commanded && transmissionsSinceOrder < SETTLED_AFTER) {
            transmissionsSinceOrder = transmissionsSinceOrder + 1;
        }
    }

    /**
     * SB-FR1: the pump does not show the ordered state in the transmission following an order,
     * or changes its state without having been ordered to.
     */
    boolean pumpFault() {
        if (!baseline) {
            return false;
        }
        PumpPosition expected = previousPosition;
        if (commanded) {
            expected = commandedPosition;
        }
        return position != expected;
    }

    /**
     * SB-FR2: in the second transmission after an order the controller does not show the
     * coming or ceasing of the flow although the pump works, or the controller changes its
     * observation while the pump did not change state and no order is in progress.
     */
    boolean controllerFault(boolean pumpSound) {
        if (!baseline || !pumpSound) {
            return false;
        }
        if (commanded && transmissionsSinceOrder == 1) {
            return false;
        }
        if (commanded && transmissionsSinceOrder == 2) {
            return flow != expectedFlow();
        }
        return flow != previousFlow && position == previousPosition;
    }

    /** No order is still taking effect: a pump shown on has finished its pressure balancing. */
    boolean settled() {
        return !commanded || transmissionsSinceOrder >= 2;
    }

    boolean indicatesOn() {
        return position == PumpPosition.PUMP_ON;
    }

    boolean indicatesFlow() {
        return flow == WaterFlow.FLOWING;
    }

    /** The program ordered the pump to the given position in this cycle's transmission. */
    void order(PumpPosition target) {
        if (!commanded || commandedPosition != target) {
            commandedPosition = target;
            commanded = true;
            transmissionsSinceOrder = 0;
        }
    }

    /** End of analysis: this transmission's readings become the previous ones. */
    void endCycle() {
        previousPosition = position;
        previousFlow = flow;
        baseline = true;
    }

    /** The pump or its controller was repaired: judge it afresh from its next readings. */
    void forget() {
        baseline = false;
        commanded = false;
        transmissionsSinceOrder = 0;
    }

    private WaterFlow expectedFlow() {
        if (commandedPosition == PumpPosition.PUMP_ON) {
            return WaterFlow.FLOWING;
        }
        return WaterFlow.NOT_FLOWING;
    }
}
