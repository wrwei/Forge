package steam_boiler.sensor;

/**
 * What the program believes about one physical unit: whether it is out of order, whether the
 * announcement of its failure has been acknowledged, and whether it reported its repair in the
 * current cycle (so that the repair is acknowledged in this cycle's transmission).
 */
public final class UnitRecord {

    private boolean outOfOrder;
    private boolean failureAcknowledged;
    private boolean repairReported;

    /** Whether the unit is currently believed out of order. */
    public boolean isOutOfOrder() {
        return outOfOrder;
    }

    /** Whether the failure detection must be (re)announced in this cycle. */
    public boolean failureAnnouncementDue() {
        return outOfOrder && !failureAcknowledged;
    }

    /** Whether the unit reported its repair in this cycle and must receive an acknowledgement. */
    public boolean repairAcknowledgementDue() {
        return repairReported;
    }

    /** The program has detected a failure of the unit. */
    public void recordFailure() {
        outOfOrder = true;
        failureAcknowledged = false;
    }

    /** The unit acknowledged the program's failure detection. */
    public void recordFailureAcknowledgement() {
        if (outOfOrder) {
            failureAcknowledged = true;
        }
    }

    /** The unit reported itself repaired; it is sound again. */
    public void recordRepair() {
        outOfOrder = false;
        failureAcknowledged = false;
        repairReported = true;
    }

    /** Clears the per-cycle repair report at the start of a receive stage. */
    public void startCycle() {
        repairReported = false;
    }
}
