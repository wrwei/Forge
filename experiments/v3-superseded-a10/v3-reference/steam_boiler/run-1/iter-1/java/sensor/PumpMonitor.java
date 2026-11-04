package steam_boiler.sensor;

import java.util.List;
import steam_boiler.SteamBoilerConstants;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.event.OutputEvent;

/**
 * What the program knows about one pump and its controller: the reports of the current
 * transmission, the state the program expects given its orders, and the two units' status.
 */
final class PumpMonitor {

    /** An order needs this many transmissions before the controller must confirm it. */
    @RoboChartType("nat") private static final int CONTROL_CONFIRMATION_DELAY = 2;

    @RoboChartType("nat") private final int pumpNumber;
    private final UnitStatus pumpStatus = new UnitStatus();
    private final UnitStatus controlStatus = new UnitStatus();

    // Reports in the current transmission.
    @RoboChartType("nat") private int stateReports = 0;
    @RoboChartType("nat") private int controlReports = 0;
    private boolean reportedOn = false;
    private boolean reportedFlowing = false;
    private boolean pumpRepairReported = false;
    private boolean controlRepairReported = false;
    private boolean pumpAckReported = false;
    private boolean controlAckReported = false;

    // History: the previous transmission and the program's orders.
    private boolean previousOn = false;
    private boolean previousPumpSound = true;
    private boolean expectedOn = false;
    private boolean orderedLastCycle = false;
    @RoboChartType("nat") private int cyclesSinceOrder = CONTROL_CONFIRMATION_DELAY;

    PumpMonitor(@RoboChartType("nat") int pumpNumber) {
        this.pumpNumber = pumpNumber;
    }

    @RoboChartType("nat")
    int pumpNumber() {
        return pumpNumber;
    }

    /** Starts a cycle: the current reports become the previous ones. */
    void startCycle() {
        previousOn = reportedOn;
        previousPumpSound = !pumpStatus.outOfOrder();
        orderedLastCycle = false;
        if (cyclesSinceOrder < CONTROL_CONFIRMATION_DELAY) {
            cyclesSinceOrder = cyclesSinceOrder + 1;
        }
        stateReports = 0;
        controlReports = 0;
        pumpRepairReported = false;
        controlRepairReported = false;
        pumpAckReported = false;
        controlAckReported = false;
    }

    /** Records an OPEN_PUMP (open) or CLOSE_PUMP order sent in the previous cycle. */
    void ordered(boolean open) {
        expectedOn = open;
        orderedLastCycle = true;
        cyclesSinceOrder = 1;
    }

    void reportState(boolean on) {
        stateReports = stateReports + 1;
        reportedOn = on;
    }

    void reportControl(boolean flowing) {
        controlReports = controlReports + 1;
        reportedFlowing = flowing;
    }

    void reportPumpRepaired() {
        pumpRepairReported = true;
    }

    void reportControlRepaired() {
        controlRepairReported = true;
    }

    void reportPumpAcknowledged() {
        pumpAckReported = true;
    }

    void reportControlAcknowledged() {
        controlAckReported = true;
    }

    /** Both indispensable messages for this pump arrived, each exactly once. */
    boolean transmissionComplete() {
        return stateReports == 1 && controlReports == 1;
    }

    /** A repair or acknowledgement message arrived that the unit's status does not explain. */
    boolean aberrant() {
        return (pumpRepairReported && !pumpStatus.outOfOrder())
                || (controlRepairReported && !controlStatus.outOfOrder())
                || (pumpAckReported && !pumpStatus.detectionPending())
                || (controlAckReported && !controlStatus.detectionPending());
    }

    /** Before the plant is waiting nothing is judged: the expectation follows the reports. */
    void track() {
        expectedOn = reportedOn;
    }

    /** Applies acknowledgements and repairs, then judges the pump and its controller. */
    void judge() {
        if (pumpAckReported) {
            pumpStatus.acknowledgeDetection();
        }
        if (controlAckReported) {
            controlStatus.acknowledgeDetection();
        }
        if (pumpRepairReported) {
            pumpStatus.repair();
            expectedOn = reportedOn;
        }
        if (controlRepairReported) {
            controlStatus.repair();
        }
        // Pump: it must follow the last order, and must not change state on its own.
        if (pumpStatus.outOfOrder()) {
            expectedOn = reportedOn;
        } else if (reportedOn != expectedOn) {
            pumpStatus.detectFailure();
            expectedOn = reportedOn;
        }
        // Controller: from the second transmission after an order on, it must see water
        // flowing exactly when the (sound) pump is on.
        boolean judgeable = !controlStatus.outOfOrder() && !pumpStatus.outOfOrder()
                && cyclesSinceOrder >= CONTROL_CONFIRMATION_DELAY;
        if (judgeable && reportedFlowing != reportedOn) {
            controlStatus.detectFailure();
        }
    }

    /** Lower bound of the flow (litre/sec) this pump gave during the cycle just ended. */
    @RoboChartType("real")
    double pastFlowLow() {
        if (!previousPumpSound) {
            return 0.0;
        }
        if (orderedLastCycle) {
            // An opened pump balances pressure for one cycle; a closed one stops at once.
            return 0.0;
        }
        if (previousOn) {
            return SteamBoilerConstants.PUMP_CAPACITY_P;
        }
        return 0.0;
    }

    /** Upper bound of the flow (litre/sec) this pump gave during the cycle just ended. */
    @RoboChartType("real")
    double pastFlowHigh() {
        if (!previousPumpSound) {
            return SteamBoilerConstants.PUMP_CAPACITY_P;
        }
        if (orderedLastCycle) {
            return 0.0;
        }
        if (previousOn) {
            return SteamBoilerConstants.PUMP_CAPACITY_P;
        }
        return 0.0;
    }

    /** Lower bound of the flow (litre/sec) this pump gives during the coming cycle. */
    @RoboChartType("real")
    double nextFlowLow() {
        if (pumpStatus.outOfOrder()) {
            return 0.0;
        }
        if (reportedOn) {
            return SteamBoilerConstants.PUMP_CAPACITY_P;
        }
        return 0.0;
    }

    /** Upper bound of the flow (litre/sec) this pump gives during the coming cycle. */
    @RoboChartType("real")
    double nextFlowHigh() {
        if (pumpStatus.outOfOrder()) {
            return SteamBoilerConstants.PUMP_CAPACITY_P;
        }
        if (reportedOn) {
            return SteamBoilerConstants.PUMP_CAPACITY_P;
        }
        return 0.0;
    }

    /** Whether the program believes the pump is pouring water now. */
    boolean believedPouring() {
        if (!pumpStatus.outOfOrder()) {
            return reportedOn;
        }
        if (!controlStatus.outOfOrder()) {
            return reportedFlowing;
        }
        return false;
    }

    boolean openable() {
        return !pumpStatus.outOfOrder() && !reportedOn;
    }

    boolean closable() {
        return !pumpStatus.outOfOrder() && reportedOn;
    }

    boolean pumpOutOfOrder() {
        return pumpStatus.outOfOrder();
    }

    boolean controlOutOfOrder() {
        return controlStatus.outOfOrder();
    }

    /** Adds this pump's failure announcements and repair acknowledgements due now. */
    void addProtocolMessages(List<OutputEvent> messages) {
        if (pumpStatus.detectionPending()) {
            messages.add(new OutputEvent.PumpFailureDetection(pumpNumber));
        }
        if (controlStatus.detectionPending()) {
            messages.add(new OutputEvent.PumpControlFailureDetection(pumpNumber));
        }
        if (pumpStatus.takeRepairAcknowledgement()) {
            messages.add(new OutputEvent.PumpRepairedAcknowledgement(pumpNumber));
        }
        if (controlStatus.takeRepairAcknowledgement()) {
            messages.add(new OutputEvent.PumpControlRepairedAcknowledgement(pumpNumber));
        }
    }
}
