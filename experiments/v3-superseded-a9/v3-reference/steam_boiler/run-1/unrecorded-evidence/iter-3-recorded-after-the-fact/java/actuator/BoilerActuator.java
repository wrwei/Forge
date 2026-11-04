package steam_boiler.actuator;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.controller.BoilerConstants;
import steam_boiler.domain.ValvePosition;
import steam_boiler.mode.OperatingMode;

/**
 * Receives the controller's output commands for the current cycle and records the last value of
 * each. The {@link Transmitter} turns them into outgoing messages at the transmit stage.
 */
public final class BoilerActuator {

    private OperatingMode announcedMode = OperatingMode.INITIALIZATION;
    private boolean modePending;
    private boolean programReadyPending;
    private ValvePosition requestedValve = ValvePosition.VALVE_CLOSED;
    @RoboChartType("nat") private int requestedPumps;
    private boolean pumpOrderPending;
    private boolean unitMessagesPending;

    /** Command: send MODE(m) in this cycle's transmission. */
    public void sendMode(OperatingMode m) {
        this.announcedMode = m;
        this.modePending = true;
    }

    /** Command: send PROGRAM_READY in this cycle's transmission. */
    public void sendProgramReady() {
        this.programReadyPending = true;
    }

    /** Command: the evacuation valve should be in {@code position}; VALVE is sent on a change. */
    public void setValve(ValvePosition position) {
        this.requestedValve = position;
    }

    /** Command: every sound pump should run. */
    public void startAllPumps() {
        this.requestedPumps = BoilerConstants.PUMP_COUNT;
        this.pumpOrderPending = true;
    }

    /** Command: every pump should stop. */
    public void stopAllPumps() {
        this.requestedPumps = 0;
        this.pumpOrderPending = true;
    }

    /** Command: exactly one sound pump should run (filling during initialization). */
    public void startFillPump() {
        this.requestedPumps = 1;
        this.pumpOrderPending = true;
    }

    /** Command: send the due failure announcements and repair acknowledgements in this cycle. */
    public void sendUnitMessages() {
        this.unitMessagesPending = true;
    }

    /** The mode most recently commanded with {@link #sendMode}. */
    public OperatingMode announcedMode() {
        return announcedMode;
    }

    /** Whether MODE is to be sent in this cycle. */
    public boolean modePending() {
        return modePending;
    }

    /** Whether PROGRAM_READY is to be sent in this cycle. */
    public boolean programReadyPending() {
        return programReadyPending;
    }

    /** The valve position most recently commanded. */
    public ValvePosition requestedValve() {
        return requestedValve;
    }

    /** The number of pumps most recently commanded to run. */
    @RoboChartType("nat")
    public int requestedPumps() {
        return requestedPumps;
    }

    /** Whether a pump order was commanded in this cycle. */
    public boolean pumpOrderPending() {
        return pumpOrderPending;
    }

    /** Whether unit messages are to be sent in this cycle. */
    public boolean unitMessagesPending() {
        return unitMessagesPending;
    }

    /** Clears this cycle's one-shot commands once they have been transmitted. */
    public void startCycle() {
        this.modePending = false;
        this.programReadyPending = false;
        this.pumpOrderPending = false;
        this.unitMessagesPending = false;
    }
}
