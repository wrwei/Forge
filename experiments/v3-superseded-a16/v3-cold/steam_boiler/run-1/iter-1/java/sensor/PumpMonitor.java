package steam_boiler.sensor;

import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.event.OutputEvent;

/**
 * One pump and its controller as the program sees them: the reports of the current and
 * the previous transmission, the last order sent, and the status of both units.
 */
final class PumpMonitor {
    @RoboChartType("nat") private final int number;
    private final UnitStatus pump = new UnitStatus();
    private final UnitStatus controller = new UnitStatus();

    @RoboChartType("nat") private int stateMessages;
    @RoboChartType("nat") private int controlMessages;
    private boolean open;
    private boolean flowing;
    private boolean pumpRepairReceived;
    private boolean controlRepairReceived;
    private boolean pumpAcknowledgementReceived;
    private boolean controlAcknowledgementReceived;

    private boolean previousOpen;
    private boolean previousFlowing;
    private boolean commandedOpen;
    private boolean orderedLastCycle;

    PumpMonitor(@RoboChartType("nat") int number) {
        this.number = number;
    }

    void beginCycle() {
        stateMessages = 0;
        controlMessages = 0;
        pumpRepairReceived = false;
        controlRepairReceived = false;
        pumpAcknowledgementReceived = false;
        controlAcknowledgementReceived = false;
        pump.beginCycle();
        controller.beginCycle();
    }

    void recordState(boolean isOpen) {
        stateMessages = stateMessages + 1;
        open = isOpen;
    }

    void recordControlState(boolean isFlowing) {
        controlMessages = controlMessages + 1;
        flowing = isFlowing;
    }

    void recordPumpRepaired() {
        pumpRepairReceived = true;
    }

    void recordControlRepaired() {
        controlRepairReceived = true;
    }

    void recordPumpAcknowledgement() {
        pumpAcknowledgementReceived = true;
    }

    void recordControlAcknowledgement() {
        controlAcknowledgementReceived = true;
    }

    /** PUMP_STATE and PUMP_CONTROL_STATE each arrived exactly once (SB-FR5). */
    boolean dataComplete() {
        return stateMessages == 1 && controlMessages == 1;
    }

    /** Applies acknowledgements, then repairs (SB-FR6, SB-FR7); false if one of them was aberrant. */
    boolean applyAcknowledgementsAndRepairs() {
        boolean consistent = true;
        if (pumpAcknowledgementReceived && !pump.acknowledgeFailure()) {
            consistent = false;
        }
        if (controlAcknowledgementReceived && !controller.acknowledgeFailure()) {
            consistent = false;
        }
        if (pumpRepairReceived && !pump.repair()) {
            consistent = false;
        }
        if (controlRepairReceived && !controller.repair()) {
            consistent = false;
        }
        return consistent;
    }

    /**
     * SB-FR1: after an order the pump must report the ordered state; without an order it must
     * not change state. SB-FR2: once the pump state has been stable for a whole cycle (two
     * transmissions after an order, covering the start-up latency) the controller must report
     * flow exactly when the pump is on; judged only while the pump itself is known to work.
     */
    void detectFailures() {
        if (pump.isReliable()) {
            boolean expectedOpen = previousOpen;
            if (orderedLastCycle) {
                expectedOpen = commandedOpen;
            }
            if (open != expectedOpen) {
                pump.detectFailure();
            }
        }
        if (controller.isReliable() && pump.isReliable() && open == previousOpen && flowing != open) {
            controller.detectFailure();
        }
    }

    /** Water this pump certainly delivered during the last cycle (litre). */
    @RoboChartType("real")
    double minimumInflow(@RoboChartType("real") double perCycle) {
        if (controller.isReliable() && flowing && previousFlowing) {
            return perCycle;
        }
        return 0.0;
    }

    /** Water this pump may have delivered during the last cycle, from the controller's reports (litre). */
    @RoboChartType("real")
    double maximumInflow(@RoboChartType("real") double perCycle) {
        if (controller.isReliable()) {
            if (flowing || previousFlowing) {
                return perCycle;
            }
            return 0.0;
        }
        return extremeInflow(perCycle);
    }

    /** Water this pump could have delivered during the last cycle under extreme behaviour (litre). */
    @RoboChartType("real")
    double extremeInflow(@RoboChartType("real") double perCycle) {
        if (pump.isReliable() && !previousOpen && !open) {
            return 0.0;
        }
        return perCycle;
    }

    /**
     * Water this pump may deliver during the next cycle (litre). A pump that is off now pours
     * nothing before the next transmission, even if opened in this cycle (SB-Beh25).
     */
    @RoboChartType("real")
    double maximumInflowNextCycle(@RoboChartType("real") double perCycle) {
        if (!pump.isFailed() && !open) {
            return 0.0;
        }
        return perCycle;
    }

    /** Whether water is now pouring from this pump into the boiler (SB-V7). */
    boolean pouring() {
        if (!controller.isFailed()) {
            return flowing;
        }
        if (!pump.isFailed()) {
            return open && previousOpen;
        }
        return false;
    }

    void commitHistory() {
        previousOpen = open;
        previousFlowing = flowing;
    }

    void clearOrder() {
        orderedLastCycle = false;
    }

    void order(boolean toOpen) {
        commandedOpen = toOpen;
        orderedLastCycle = true;
    }

    boolean pumpFailed() {
        return pump.isFailed();
    }

    boolean controllerFailed() {
        return controller.isFailed();
    }

    /** Failure announcements awaiting acknowledgement and acknowledgements of this cycle's repairs. */
    void addStatusMessages(List<OutputEvent> messages) {
        if (pump.isAwaitingAcknowledgement()) {
            messages.add(new OutputEvent.PumpFailureDetection(number));
        }
        if (controller.isAwaitingAcknowledgement()) {
            messages.add(new OutputEvent.PumpControlFailureDetection(number));
        }
        if (pump.wasRepairedThisCycle()) {
            messages.add(new OutputEvent.PumpRepairedAcknowledgement(number));
        }
        if (controller.wasRepairedThisCycle()) {
            messages.add(new OutputEvent.PumpControlRepairedAcknowledgement(number));
        }
    }
}
