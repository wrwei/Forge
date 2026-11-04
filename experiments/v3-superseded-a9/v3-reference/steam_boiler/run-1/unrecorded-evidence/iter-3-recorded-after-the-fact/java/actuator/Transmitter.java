package steam_boiler.actuator;

import java.util.ArrayList;
import java.util.List;
import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.domain.ValvePosition;
import steam_boiler.event.OutputEvent;
import steam_boiler.mode.OperatingMode;
import steam_boiler.sensor.UnitRecord;
import steam_boiler.sensor.UnitStatusTable;

/**
 * The transmit stage: turns the controller's commands for the cycle into the outgoing messages,
 * which are all emitted together. It keeps the pump-order log and the valve position, which the
 * receive stage needs to judge the pumps and the level readings.
 */
public final class Transmitter {

    private final BoilerActuator actuator;
    private final UnitStatusTable units;
    private final List<PumpOrder> orders = new ArrayList<>();
    private ValvePosition valve = ValvePosition.VALVE_CLOSED;
    private OperatingMode lastMode = OperatingMode.INITIALIZATION;
    private boolean programReadySent;

    /** Creates the transmit stage for the given command recorder and unit table. */
    public Transmitter(BoilerActuator actuator, UnitStatusTable units) {
        this.actuator = actuator;
        this.units = units;
        for (int i = 0; i < BoilerConstants.PUMP_COUNT; i++) {
            orders.add(new PumpOrder());
        }
    }

    /** Builds and returns this cycle's outgoing transmission, then clears the cycle's commands. */
    public List<OutputEvent> transmit() {
        List<OutputEvent> outgoing = new ArrayList<>();
        if (actuator.modePending()) {
            outgoing.add(new OutputEvent.Mode(actuator.announcedMode()));
            lastMode = actuator.announcedMode();
        }
        if (actuator.programReadyPending()) {
            outgoing.add(new OutputEvent.ProgramReady());
            programReadySent = true;
        }
        if (actuator.requestedValve() != valve) {
            outgoing.add(new OutputEvent.Valve());
            valve = actuator.requestedValve();
        }
        if (actuator.pumpOrderPending()) {
            composePumpOrders(actuator.requestedPumps(), outgoing);
        }
        if (actuator.unitMessagesPending()) {
            composeUnitMessages(outgoing);
        }
        for (int i = 0; i < orders.size(); i++) {
            orders.get(i).advance();
        }
        actuator.startCycle();
        return outgoing;
    }

    /** The operating mode announced by the last MODE message sent. */
    public OperatingMode lastMode() {
        return lastMode;
    }

    /** Whether PROGRAM_READY has been sent at least once. */
    public boolean programReadySent() {
        return programReadySent;
    }

    /** The current position of the evacuation valve, as last ordered. */
    public ValvePosition valvePosition() {
        return valve;
    }

    /** Whether pump {@code pump} (1 to 4) was last ordered on. */
    public boolean pumpOrderedOn(@RoboChartType("nat") int pump) {
        return orders.get(pump - 1).orderedOn();
    }

    /** How many transmissions have been received since the last order to pump {@code pump}, at most 2. */
    @RoboChartType("nat")
    public int transmissionsSinceOrder(@RoboChartType("nat") int pump) {
        return orders.get(pump - 1).transmissionsSinceOrder();
    }

    private void composePumpOrders(@RoboChartType("nat") int count, List<OutputEvent> outgoing) {
        List<Boolean> wanted = new ArrayList<>();
        int remaining = count;
        for (int i = 0; i < orders.size(); i++) {
            boolean keep = remaining > 0 && orders.get(i).orderedOn() && !units.pump(i + 1).isOutOfOrder();
            if (keep) {
                remaining = remaining - 1;
            }
            wanted.add(keep);
        }
        for (int i = 0; i < orders.size(); i++) {
            boolean start = remaining > 0 && !wanted.get(i) && !units.pump(i + 1).isOutOfOrder();
            if (start) {
                remaining = remaining - 1;
                wanted.set(i, true);
            }
        }
        for (int i = 0; i < orders.size(); i++) {
            PumpOrder order = orders.get(i);
            boolean on = wanted.get(i);
            if (on != order.orderedOn()) {
                if (on) {
                    outgoing.add(new OutputEvent.OpenPump(i + 1));
                } else {
                    outgoing.add(new OutputEvent.ClosePump(i + 1));
                }
                order.issue(on);
            }
        }
    }

    private void composeUnitMessages(List<OutputEvent> outgoing) {
        for (int pump = 1; pump <= BoilerConstants.PUMP_COUNT; pump++) {
            UnitRecord pumpRecord = units.pump(pump);
            if (pumpRecord.failureAnnouncementDue()) {
                outgoing.add(new OutputEvent.PumpFailureDetection(pump));
            }
            if (pumpRecord.repairAcknowledgementDue()) {
                outgoing.add(new OutputEvent.PumpRepairedAcknowledgement(pump));
            }
            UnitRecord controlRecord = units.pumpControl(pump);
            if (controlRecord.failureAnnouncementDue()) {
                outgoing.add(new OutputEvent.PumpControlFailureDetection(pump));
            }
            if (controlRecord.repairAcknowledgementDue()) {
                outgoing.add(new OutputEvent.PumpControlRepairedAcknowledgement(pump));
            }
        }
        if (units.levelDevice().failureAnnouncementDue()) {
            outgoing.add(new OutputEvent.LevelFailureDetection());
        }
        if (units.levelDevice().repairAcknowledgementDue()) {
            outgoing.add(new OutputEvent.LevelRepairedAcknowledgement());
        }
        if (units.steamDevice().failureAnnouncementDue()) {
            outgoing.add(new OutputEvent.SteamFailureDetection());
        }
        if (units.steamDevice().repairAcknowledgementDue()) {
            outgoing.add(new OutputEvent.SteamRepairedAcknowledgement());
        }
    }
}
