package steam_boiler.sensor;

/**
 * Status of one physical unit (a pump, a pump controller, the level device or
 * the steam device) as the program believes it (SB-V9): sound or out of order,
 * whether the announced failure has been acknowledged, and whether a repair
 * acknowledgement is to be sent in the current cycle (SB-FR6, SB-FR7).
 */
final class UnitStatus {

    private boolean outOfOrder;
    private boolean awaitingAcknowledgement;
    private boolean repairAcknowledgementDue;

    /** The program has detected a failure of the unit. */
    void detectFailure() {
        outOfOrder = true;
        awaitingAcknowledgement = true;
    }

    /** The unit acknowledged the failure detection. */
    void acknowledgeFailure() {
        awaitingAcknowledgement = false;
    }

    /** The unit reported itself repaired: sound again, and to be acknowledged. */
    void repair() {
        outOfOrder = false;
        awaitingAcknowledgement = false;
        repairAcknowledgementDue = true;
    }

    boolean isOutOfOrder() {
        return outOfOrder;
    }

    boolean isAwaitingAcknowledgement() {
        return awaitingAcknowledgement;
    }

    /** Returns whether a repair acknowledgement is due, and clears it. */
    boolean takeRepairAcknowledgement() {
        boolean due = repairAcknowledgementDue;
        repairAcknowledgementDue = false;
        return due;
    }
}
