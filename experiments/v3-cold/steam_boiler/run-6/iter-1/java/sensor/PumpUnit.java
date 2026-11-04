package steam_boiler.sensor;

import java.util.List;
import steam_boiler.BoilerConstants;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.PumpId;
import steam_boiler.event.OutputEvent;

/**
 * Everything the program knows about one pump and its controller: the
 * current cycle's reports, the previous cycle's reports, the orders of the
 * last two transmissions, and the failure bookkeeping of both units.
 */
final class PumpUnit {

    private final PumpId id;
    private final UnitStatus pump = new UnitStatus();
    private final UnitStatus control = new UnitStatus();

    @RoboChartType("nat") private int stateMessages = 0;
    @RoboChartType("nat") private int controlMessages = 0;
    private boolean switchedOn = false;
    private boolean waterFlowing = false;
    private boolean pumpRepairedReceived = false;
    private boolean controlRepairedReceived = false;
    private boolean pumpAcknowledgementReceived = false;
    private boolean controlAcknowledgementReceived = false;

    private boolean surelyPouring = false;
    private boolean possiblyPouring = false;

    private boolean previousKnown = false;
    private boolean previousSwitchedOn = false;
    private boolean previousFlowing = false;
    private boolean previousSurelyPouring = false;
    private boolean previousPossiblyPouring = false;

    private boolean openOrderedLast = false;
    private boolean closeOrderedLast = false;
    private boolean openOrderedBefore = false;
    private boolean closeOrderedBefore = false;

    PumpUnit(PumpId id) {
        this.id = id;
    }

    void beginCycle() {
        stateMessages = 0;
        controlMessages = 0;
        pumpRepairedReceived = false;
        controlRepairedReceived = false;
        pumpAcknowledgementReceived = false;
        controlAcknowledgementReceived = false;
        pump.beginCycle();
        control.beginCycle();
    }

    void receiveState(boolean on) {
        stateMessages = stateMessages + 1;
        switchedOn = on;
    }

    void receiveControlState(boolean flowing) {
        controlMessages = controlMessages + 1;
        waterFlowing = flowing;
    }

    void receivePumpRepaired() {
        pumpRepairedReceived = true;
    }

    void receiveControlRepaired() {
        controlRepairedReceived = true;
    }

    void receivePumpAcknowledgement() {
        pumpAcknowledgementReceived = true;
    }

    void receiveControlAcknowledgement() {
        controlAcknowledgementReceived = true;
    }

    /** PUMP_STATE and PUMP_CONTROL_STATE each arrived exactly once. */
    boolean dataComplete() {
        return stateMessages == 1 && controlMessages == 1;
    }

    /** Shifts in the orders the last transmission gave this pump. */
    void recordOrders(boolean open, boolean close) {
        openOrderedBefore = openOrderedLast;
        closeOrderedBefore = closeOrderedLast;
        openOrderedLast = open;
        closeOrderedLast = close;
    }

    /**
     * Applies this cycle's failure acknowledgements.
     *
     * @return false when an acknowledgement answers no announcement
     */
    boolean processAcknowledgements() {
        boolean valid = true;
        if (pumpAcknowledgementReceived && !pump.acknowledgeFailure()) {
            valid = false;
        }
        if (controlAcknowledgementReceived && !control.acknowledgeFailure()) {
            valid = false;
        }
        return valid;
    }

    void processRepairs() {
        if (pumpRepairedReceived) {
            pump.repair();
        }
        if (controlRepairedReceived) {
            control.repair();
        }
    }

    /** Pump failure, pump-controller failure, then what the pump pours. */
    void detectFailures() {
        detectPumpFailure();
        detectControlFailure();
        if (!control.failed()) {
            surelyPouring = waterFlowing;
            possiblyPouring = waterFlowing;
        } else {
            surelyPouring = false;
            possiblyPouring = switchedOn || pump.failed();
        }
    }

    /**
     * The pump has failed when, in the transmission following an order, its
     * state does not show the order carried out, or when its state changes
     * without an order.
     */
    private void detectPumpFailure() {
        if (pump.failed() || pump.repairedThisCycle()) {
            return;
        }
        boolean expectationKnown = previousKnown;
        boolean expectedOn = previousSwitchedOn;
        if (openOrderedLast) {
            expectationKnown = true;
            expectedOn = true;
        } else if (closeOrderedLast) {
            expectationKnown = true;
            expectedOn = false;
        }
        if (expectationKnown && switchedOn != expectedOn) {
            pump.detectFailure();
        }
    }

    /**
     * The pump controller has failed when, in the second transmission after an
     * order to a sound pump, it does not show the flow starting or stopping,
     * or when its observation changes with no order in the last two
     * transmissions. A later contrary order supersedes the earlier one.
     */
    private void detectControlFailure() {
        if (control.failed() || control.repairedThisCycle() || pump.failed()) {
            return;
        }
        boolean orderedBefore = openOrderedBefore || closeOrderedBefore;
        boolean orderedLast = openOrderedLast || closeOrderedLast;
        boolean superseded = (openOrderedBefore && closeOrderedLast) || (closeOrderedBefore && openOrderedLast);
        if (orderedBefore && !superseded) {
            if (waterFlowing != openOrderedBefore) {
                control.detectFailure();
            }
        } else if (!orderedBefore && !orderedLast && previousKnown && waterFlowing != previousFlowing) {
            control.detectFailure();
        }
    }

    /** Least water this pump can have supplied since the last cycle (litre). */
    @RoboChartType("real")
    double minimalInflow() {
        if (previousKnown && previousSurelyPouring && surelyPouring) {
            return BoilerConstants.PUMP_CAPACITY * BoilerConstants.CYCLE_DURATION;
        }
        return 0.0;
    }

    /**
     * Most water this pump can have supplied since the last cycle (litre). A
     * sound pump ordered off stopped at once; a sound pump ordered on that was
     * not already pouring spent the cycle balancing pressure.
     */
    @RoboChartType("real")
    double maximalInflow() {
        if (!previousKnown) {
            return BoilerConstants.PUMP_CAPACITY * BoilerConstants.CYCLE_DURATION;
        }
        if (!pump.failed() && closeOrderedLast) {
            return 0.0;
        }
        if (!pump.failed() && openOrderedLast && !previousPossiblyPouring) {
            return 0.0;
        }
        if (previousPossiblyPouring || possiblyPouring) {
            return BoilerConstants.PUMP_CAPACITY * BoilerConstants.CYCLE_DURATION;
        }
        return 0.0;
    }

    /**
     * Whether the pump may pour during the coming cycle: it pours now, or it
     * is on and finishing its start-up. A pump ordered on in this cycle pours
     * nothing during the coming cycle, which is still pressure balancing.
     */
    boolean mayPourNext() {
        return possiblyPouring || switchedOn;
    }

    /** Best judgement of whether the pump is pouring now. */
    boolean pouringNow() {
        if (!control.failed()) {
            return waterFlowing;
        }
        return switchedOn;
    }

    boolean pumpFailed() {
        return pump.failed();
    }

    boolean controlFailed() {
        return control.failed();
    }

    void remember() {
        previousKnown = true;
        previousSwitchedOn = switchedOn;
        previousFlowing = waterFlowing;
        previousSurelyPouring = surelyPouring;
        previousPossiblyPouring = possiblyPouring;
    }

    /** Adds this cycle's failure announcements and repair acknowledgements. */
    void addStatusMessages(List<OutputEvent> messages) {
        if (pump.announcementDue()) {
            messages.add(new OutputEvent.PumpFailureDetection(id));
        }
        if (control.announcementDue()) {
            messages.add(new OutputEvent.PumpControlFailureDetection(id));
        }
        if (pump.takeRepairAcknowledgement()) {
            messages.add(new OutputEvent.PumpRepairedAcknowledgement(id));
        }
        if (control.takeRepairAcknowledgement()) {
            messages.add(new OutputEvent.PumpControlRepairedAcknowledgement(id));
        }
    }
}
