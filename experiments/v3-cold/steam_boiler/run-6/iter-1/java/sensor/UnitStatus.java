package steam_boiler.sensor;

/**
 * Failure and repair bookkeeping for one physical unit: whether it is believed
 * out of order, whether a failure detection announced for it still awaits its
 * acknowledgement, and whether a repair acknowledgement is to be sent.
 */
final class UnitStatus {

    private boolean failed = false;
    private boolean announcementOutstanding = false;
    private boolean repairAcknowledgementDue = false;
    private boolean repairedThisCycle = false;

    void beginCycle() {
        repairedThisCycle = false;
    }

    boolean failed() {
        return failed;
    }

    boolean repairedThisCycle() {
        return repairedThisCycle;
    }

    /** The program has detected a failure of the unit. */
    void detectFailure() {
        if (!failed) {
            failed = true;
            announcementOutstanding = true;
        }
    }

    /** Whether the failure detection must be (re)announced this cycle. */
    boolean announcementDue() {
        return failed && announcementOutstanding;
    }

    /**
     * Records the unit's acknowledgement of a failure detection.
     *
     * @return false when no announcement awaited an acknowledgement, which
     *         makes the acknowledgement aberrant
     */
    boolean acknowledgeFailure() {
        if (!announcementOutstanding) {
            return false;
        }
        announcementOutstanding = false;
        return true;
    }

    /** The unit reports itself repaired: it is sound again and is answered. */
    void repair() {
        if (failed) {
            failed = false;
            repairedThisCycle = true;
        }
        repairAcknowledgementDue = true;
    }

    /** Whether a repair acknowledgement is due; clears the obligation. */
    boolean takeRepairAcknowledgement() {
        boolean due = repairAcknowledgementDue;
        repairAcknowledgementDue = false;
        return due;
    }
}
