package steam_boiler.sensor;

import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.PumpId;
import steam_boiler.event.OutputEvent;

/**
 * One pump and its pump controller: the messages received about them in the current cycle, the
 * state the program expects them to be in, their failure detection, and whether the pump can be
 * counted on to pour water during the cycle that follows the latest analysis.
 */
final class PumpMonitor {
    /** Number of transmissions after an order by which the pump controller must reflect it. */
    @RoboChartType("nat") private static final int CONTROL_DELAY = 2;

    private final PumpId pump;
    private final UnitStatus pumpStatus = new UnitStatus();
    private final UnitStatus controlStatus = new UnitStatus();

    @RoboChartType("nat") private int stateMessages = 0;
    @RoboChartType("nat") private int controlMessages = 0;
    private boolean reportedOn = false;
    private boolean reportedFlow = false;
    private boolean repairedInbox = false;
    private boolean controlRepairedInbox = false;
    private boolean failureAckInbox = false;
    private boolean controlFailureAckInbox = false;

    private boolean judging = false;
    private boolean expectedOn = false;
    @RoboChartType("nat") private int cyclesSinceChange = CONTROL_DELAY;
    private boolean surelyPouring = false;
    private boolean possiblyPouring = true;
    private boolean repairAckDue = false;
    private boolean controlRepairAckDue = false;

    PumpMonitor(PumpId pump) {
        this.pump = pump;
    }

    void receiveState(boolean on) {
        stateMessages = stateMessages + 1;
        reportedOn = on;
    }

    void receiveControlState(boolean flowing) {
        controlMessages = controlMessages + 1;
        reportedFlow = flowing;
    }

    void receiveRepaired() {
        repairedInbox = true;
    }

    void receiveControlRepaired() {
        controlRepairedInbox = true;
    }

    void receiveFailureAcknowledgement() {
        failureAckInbox = true;
    }

    void receiveControlFailureAcknowledgement() {
        controlFailureAckInbox = true;
    }

    /** PUMP_STATE and PUMP_CONTROL_STATE must each arrive exactly once per transmission. */
    boolean dataComplete() {
        return stateMessages == 1 && controlMessages == 1;
    }

    /** Applies acknowledgements and repairs; false if one of them was aberrant. */
    boolean processProtocol() {
        boolean consistent = true;
        repairAckDue = false;
        controlRepairAckDue = false;
        if (failureAckInbox && !pumpStatus.acknowledgeDetection()) {
            consistent = false;
        }
        if (controlFailureAckInbox && !controlStatus.acknowledgeDetection()) {
            consistent = false;
        }
        if (repairedInbox) {
            if (pumpStatus.markRepaired()) {
                repairAckDue = true;
                expectedOn = reportedOn;
                cyclesSinceChange = CONTROL_DELAY;
            } else {
                consistent = false;
            }
        }
        if (controlRepairedInbox) {
            if (controlStatus.markRepaired()) {
                controlRepairAckDue = true;
            } else {
                consistent = false;
            }
        }
        return consistent;
    }

    /**
     * Judges the pump (one transmission after an order) and its controller (two transmissions
     * after an order, the pump's start-up latency), then decides how the pump can be counted on
     * during the coming cycle.
     */
    void judge(boolean monitoring) {
        if (cyclesSinceChange < CONTROL_DELAY) {
            cyclesSinceChange = cyclesSinceChange + 1;
        }
        if (!judging) {
            expectedOn = reportedOn;
            cyclesSinceChange = CONTROL_DELAY;
            judging = monitoring;
        } else {
            if (pumpStatus.sound() && reportedOn != expectedOn) {
                pumpStatus.markFailed();
            }
            if (pumpStatus.sound() && controlStatus.sound() && cyclesSinceChange >= CONTROL_DELAY
                    && reportedFlow != expectedOn) {
                controlStatus.markFailed();
            }
        }
        surelyPouring = pumpStatus.sound() && reportedOn
                && (reportedFlow || cyclesSinceChange < CONTROL_DELAY || !controlStatus.sound());
        possiblyPouring = !pumpStatus.sound() || reportedOn || reportedFlow;
    }

    /** Records an OPEN_PUMP (true) or CLOSE_PUMP (false) order; closing takes effect at once. */
    void ordered(boolean on) {
        if (on != expectedOn) {
            expectedOn = on;
            cyclesSinceChange = 0;
        }
        if (!on) {
            surelyPouring = false;
            possiblyPouring = !pumpStatus.sound();
        }
    }

    boolean pumpSound() {
        return pumpStatus.sound();
    }

    boolean controlSound() {
        return controlStatus.sound();
    }

    boolean surelyPouring() {
        return surelyPouring;
    }

    boolean possiblyPouring() {
        return possiblyPouring;
    }

    void addProtocolMessages(List<OutputEvent> messages) {
        if (pumpStatus.announcementPending()) {
            messages.add(new OutputEvent.PumpFailureDetection(pump));
        }
        if (controlStatus.announcementPending()) {
            messages.add(new OutputEvent.PumpControlFailureDetection(pump));
        }
        if (repairAckDue) {
            messages.add(new OutputEvent.PumpRepairedAcknowledgement(pump));
        }
        if (controlRepairAckDue) {
            messages.add(new OutputEvent.PumpControlRepairedAcknowledgement(pump));
        }
    }

    void clearInbox() {
        stateMessages = 0;
        controlMessages = 0;
        repairedInbox = false;
        controlRepairedInbox = false;
        failureAckInbox = false;
        controlFailureAckInbox = false;
    }
}
