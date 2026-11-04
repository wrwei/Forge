package steam_boiler.monitor;

/**
 * Status of one physical unit (SB-V9): whether it is believed sound or out of order,
 * and whether the failure detection announced for it has been acknowledged (SB-FR6).
 */
final class UnitHealth {

    private boolean outOfOrder = false;
    private boolean failureAcknowledged = false;

    boolean sound() {
        return !outOfOrder;
    }

    /** The program has detected a failure of the unit; the detection must be announced. */
    void fail() {
        outOfOrder = true;
        failureAcknowledged = false;
    }

    /** The unit has acknowledged the failure detection (SB-FR6). */
    void acknowledgeFailure() {
        if (outOfOrder) {
            failureAcknowledged = true;
        }
    }

    /** The unit reported itself repaired (SB-FR7). */
    void repair() {
        outOfOrder = false;
        failureAcknowledged = false;
    }

    /** The failure detection must be (re)announced in this cycle (SB-FR6). */
    boolean announcementDue() {
        return outOfOrder && !failureAcknowledged;
    }
}
