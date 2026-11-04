package steam_boiler.monitor;

import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.PumpPosition;
import steam_boiler.domain.WaterFlow;
import steam_boiler.event.OutputEvent;

/**
 * One pump and its pump controller: the reports received about them, the orders the
 * program sent to the pump, and the status of both units (SB-V9). Implements the pump
 * and pump-controller failure judgements (SB-FR1, SB-FR2) and the pumps' contribution
 * to level predictions, including the start-up latency (SB-Beh25).
 */
final class PumpUnit {

    @RoboChartType("nat") private final int number;
    private final UnitHealth pumpHealth = new UnitHealth();
    private final UnitHealth controllerHealth = new UnitHealth();

    @RoboChartType("nat") private int positionReports = 0;
    @RoboChartType("nat") private int flowReports = 0;
    private PumpPosition reportedPosition = PumpPosition.OFF;
    private WaterFlow reportedFlow = WaterFlow.NO_FLOW;
    private PumpPosition previousPosition = PumpPosition.OFF;
    private WaterFlow previousFlow = WaterFlow.NO_FLOW;
    private boolean history = false;

    // Orders sent in the previous transmission and in the one before it.
    private PumpOrder lastOrder = PumpOrder.NO_ORDER;
    private PumpOrder orderBeforeLast = PumpOrder.NO_ORDER;

    private boolean pumpRepairReported = false;
    private boolean controllerRepairReported = false;
    private boolean pumpAcknowledgementReported = false;
    private boolean controllerAcknowledgementReported = false;
    private boolean pumpFresh = false;
    private boolean controllerFresh = false;

    PumpUnit(@RoboChartType("nat") int number) {
        this.number = number;
    }

    int number() {
        return number;
    }

    // --- reception -------------------------------------------------------------

    void beginTransmission() {
        positionReports = 0;
        flowReports = 0;
        pumpRepairReported = false;
        controllerRepairReported = false;
        pumpAcknowledgementReported = false;
        controllerAcknowledgementReported = false;
        pumpFresh = false;
        controllerFresh = false;
    }

    void reportPosition(PumpPosition position) {
        positionReports = positionReports + 1;
        reportedPosition = position;
    }

    void reportFlow(WaterFlow flow) {
        flowReports = flowReports + 1;
        reportedFlow = flow;
    }

    void reportPumpRepaired() {
        pumpRepairReported = true;
    }

    void reportControllerRepaired() {
        controllerRepairReported = true;
    }

    void reportPumpAcknowledgement() {
        pumpAcknowledgementReported = true;
    }

    void reportControllerAcknowledgement() {
        controllerAcknowledgementReported = true;
    }

    /** PUMP_STATE and PUMP_CONTROL_STATE each arrived exactly once (SB-FR5). */
    boolean reportsComplete() {
        return positionReports == 1 && flowReports == 1;
    }

    // --- analysis --------------------------------------------------------------

    void handleAcknowledgements() {
        if (pumpAcknowledgementReported) {
            pumpHealth.acknowledgeFailure();
        }
        if (controllerAcknowledgementReported) {
            controllerHealth.acknowledgeFailure();
        }
    }

    void handleRepairs(List<OutputEvent> acknowledgements) {
        if (pumpRepairReported) {
            pumpHealth.repair();
            pumpFresh = true;
            acknowledgements.add(new OutputEvent.PumpRepairedAcknowledgement(number));
        }
        if (controllerRepairReported) {
            controllerHealth.repair();
            controllerFresh = true;
            acknowledgements.add(new OutputEvent.PumpControlRepairedAcknowledgement(number));
        }
    }

    /** Judges the pump (SB-FR1) and then its controller (SB-FR2). */
    void checkReports() {
        if (pumpHealth.sound() && !pumpFresh && pumpReactionWrong()) {
            pumpHealth.fail();
        }
        boolean pumpKnownSound = pumpHealth.sound() && !pumpFresh;
        if (controllerHealth.sound() && !controllerFresh && pumpKnownSound && flowReactionWrong()) {
            controllerHealth.fail();
        }
    }

    // SB-FR1: after an order the pump must show the ordered state in the following
    // transmission; without an order it must not change state.
    private boolean pumpReactionWrong() {
        if (lastOrder == PumpOrder.OPEN_ORDER) {
            return reportedPosition != PumpPosition.ON;
        }
        if (lastOrder == PumpOrder.CLOSE_ORDER) {
            return reportedPosition != PumpPosition.OFF;
        }
        return history && reportedPosition != previousPosition;
    }

    // SB-FR2: the flow must follow an order in the second transmission after it; with
    // no order in the last two transmissions the flow must not change.
    private boolean flowReactionWrong() {
        if (orderBeforeLast == PumpOrder.OPEN_ORDER && lastOrder != PumpOrder.CLOSE_ORDER) {
            return reportedFlow != WaterFlow.FLOWING;
        }
        if (orderBeforeLast == PumpOrder.CLOSE_ORDER && lastOrder != PumpOrder.OPEN_ORDER) {
            return reportedFlow != WaterFlow.NO_FLOW;
        }
        if (lastOrder == PumpOrder.NO_ORDER && orderBeforeLast == PumpOrder.NO_ORDER) {
            return history && reportedFlow != previousFlow;
        }
        return false;
    }

    // --- level prediction ----------------------------------------------------------

    /** The pump may have poured water during the cycle just elapsed. */
    boolean possiblyPouredLastCycle() {
        if (!history || !pumpHealth.sound()) {
            return true;
        }
        if (lastOrder == PumpOrder.CLOSE_ORDER) {
            return false;
        }
        // A pump switched on in the previous transmission was still balancing (SB-Beh25).
        return previousPosition == PumpPosition.ON;
    }

    /**
     * The pump certainly poured water during the cycle just elapsed: it was on at its
     * start, was not closed, and its controller now confirms the flow.
     */
    boolean surelyPouredLastCycle() {
        return history
                && pumpHealth.sound()
                && controllerHealth.sound()
                && !controllerFresh
                && lastOrder != PumpOrder.CLOSE_ORDER
                && previousPosition == PumpPosition.ON
                && reportedFlow == WaterFlow.FLOWING;
    }

    /** The pump may pour water during the coming cycle; closing takes effect at once. */
    boolean possiblyPoursNextCycle(boolean closing) {
        if (!pumpHealth.sound()) {
            return true;
        }
        if (closing) {
            return false;
        }
        return reportedPosition == PumpPosition.ON;
    }

    /**
     * The pump will pour water during the coming cycle: it is sound, on, and not being
     * closed. A pump switched on now pours nothing for a cycle (SB-Beh25), so only a
     * pump already on counts.
     */
    boolean surelyPoursNextCycle(boolean closing) {
        return pumpHealth.sound() && !closing && reportedPosition == PumpPosition.ON;
    }

    /** The pump is on and its controller sees water flowing (SB-V7). */
    boolean delivering() {
        return reportedPosition == PumpPosition.ON && reportedFlow == WaterFlow.FLOWING;
    }

    void endTransmission() {
        previousPosition = reportedPosition;
        previousFlow = reportedFlow;
        history = true;
    }

    // --- orders and status -----------------------------------------------------------

    void shiftOrders() {
        orderBeforeLast = lastOrder;
        lastOrder = PumpOrder.NO_ORDER;
    }

    void recordOrder(PumpOrder order) {
        lastOrder = order;
    }

    boolean pumpSound() {
        return pumpHealth.sound();
    }

    boolean controllerSound() {
        return controllerHealth.sound();
    }

    boolean pumpAnnouncementDue() {
        return pumpHealth.announcementDue();
    }

    boolean controllerAnnouncementDue() {
        return controllerHealth.announcementDue();
    }
}
