package steam_boiler.sensor;

/**
 * What the program believes about one physical unit: sound or out of order, whether the
 * announced failure detection has been acknowledged, and whether a repair awaits its
 * acknowledgement.
 */
final class UnitStatus {

    private boolean outOfOrder = false;
    private boolean failureAcknowledged = true;
    private boolean repairAcknowledgementDue = false;

    boolean outOfOrder() {
        return outOfOrder;
    }

    /** The failure detection must be announced again: the unit failed and no ack arrived. */
    boolean detectionPending() {
        return outOfOrder && !failureAcknowledged;
    }

    void detectFailure() {
        outOfOrder = true;
        failureAcknowledged = false;
    }

    void acknowledgeDetection() {
        failureAcknowledged = true;
    }

    void repair() {
        outOfOrder = false;
        failureAcknowledged = true;
        repairAcknowledgementDue = true;
    }

    /** Whether a repair acknowledgement is due now; it is due once per repair. */
    boolean takeRepairAcknowledgement() {
        boolean due = repairAcknowledgementDue;
        repairAcknowledgementDue = false;
        return due;
    }
}
