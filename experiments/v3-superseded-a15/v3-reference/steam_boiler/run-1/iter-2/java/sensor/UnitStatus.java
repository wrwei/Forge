package steam_boiler.sensor;

/**
 * Bookkeeping for one physical unit (SB-V9): whether it is believed sound or
 * out of order, whether the announced failure detection still awaits its
 * acknowledgement (SB-FR6), and whether a repair notification received in the
 * current cycle must be acknowledged (SB-FR7).
 */
public final class UnitStatus {

    private boolean outOfOrder;
    private boolean detectionPending;
    private boolean repairAcknowledgementDue;
    private boolean changedThisCycle;

    void beginCycle() {
        repairAcknowledgementDue = false;
        changedThisCycle = false;
    }

    void detectFailure() {
        outOfOrder = true;
        detectionPending = true;
        changedThisCycle = true;
    }

    void acknowledgeDetection() {
        detectionPending = false;
    }

    void repair() {
        outOfOrder = false;
        detectionPending = false;
        repairAcknowledgementDue = true;
        changedThisCycle = true;
    }

    boolean changedThisCycle() {
        return changedThisCycle;
    }

    /** True while the unit is believed out of order. */
    public boolean outOfOrder() {
        return outOfOrder;
    }

    /** True while the failure detection announced for the unit awaits acknowledgement. */
    public boolean detectionPending() {
        return detectionPending;
    }

    /** True in the cycle in which the unit's repair notification was received. */
    public boolean repairAcknowledgementDue() {
        return repairAcknowledgementDue;
    }
}
