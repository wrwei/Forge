package steam_boiler.sensor;

/**
 * One pump and its controller: their soundness (SB-V9), the state the program last
 * ordered, and the failure judgements of SB-FR1 and SB-FR2. Pumps are assumed to
 * be off, with no order pending, when the program starts.
 */
final class PumpUnit {

    /** Number of transmissions after an order once the pump's flow has settled. */
    private static final int SETTLED = 2;

    final UnitRecord pump = new UnitRecord();
    final UnitRecord control = new UnitRecord();

    private boolean expectedOn = false;
    private int transmissionsSinceOrder = SETTLED;
    private int stateMessages;
    private boolean reportedOn;
    private int controlMessages;
    private boolean reportedFlow;

    /** Starts a new transmission: one more transmission has followed the last order. */
    void beginTransmission() {
        pump.beginTransmission();
        control.beginTransmission();
        stateMessages = 0;
        controlMessages = 0;
        if (transmissionsSinceOrder < SETTLED) {
            transmissionsSinceOrder = transmissionsSinceOrder + 1;
        }
    }

    void receiveState(boolean on) {
        stateMessages = stateMessages + 1;
        reportedOn = on;
    }

    void receiveControl(boolean flowing) {
        controlMessages = controlMessages + 1;
        reportedFlow = flowing;
    }

    /** Whether exactly one PUMP_STATE and one PUMP_CONTROL_STATE arrived for this pump (SB-FR5). */
    boolean complete() {
        return stateMessages == 1 && controlMessages == 1;
    }

    /**
     * Applies acknowledgement and repair notices. A repaired pump is taken to be in
     * the state it now reports, and its flow is given one transmission to settle.
     *
     * @return false if a notice was aberrant
     */
    boolean settleNotices() {
        boolean pumpConsistent = pump.settleNotices();
        boolean controlConsistent = control.settleNotices();
        if (pump.repairAcknowledgementDue() && stateMessages == 1) {
            expectedOn = reportedOn;
            transmissionsSinceOrder = 1;
        }
        return pumpConsistent && controlConsistent;
    }

    /**
     * SB-FR1: from the first transmission after an order, the pump must report the
     * ordered state; a state that differs from the last order is either an order not
     * obeyed or a spontaneous change. SB-FR2: from the second transmission after an
     * order, a controller whose pump works correctly must report flow exactly when
     * the pump is on (the extra transmission covers the start-up latency, SB-Beh25).
     */
    void detectFailures() {
        if (pump.sound() && transmissionsSinceOrder >= 1 && reportedOn != expectedOn) {
            pump.detectFailure();
        }
        if (control.sound() && pump.sound() && transmissionsSinceOrder >= SETTLED
                && reportedFlow != expectedOn) {
            control.detectFailure();
        }
    }

    /** Whether the pump is known to be pouring water (both it and its controller sound). */
    boolean knownPouring() {
        return pump.sound() && control.sound() && reportedFlow;
    }

    /** Whether the pump may be pouring water, assuming the worst of an unsound unit. */
    boolean mayPour() {
        return reportedOn || reportedFlow || !pump.sound() || !control.sound();
    }

    /** Notes an order sent to this pump in the current cycle. */
    void recordOrder(boolean on) {
        if (on != expectedOn) {
            expectedOn = on;
            transmissionsSinceOrder = 0;
        }
    }
}
