package steam_boiler.monitor;

/*
 * What the program believes about one physical unit (SB-V9): whether it is out of order, whether
 * the announcement of its failure still awaits acknowledgement (SB-FR6), and whether a repair it
 * reported still has to be acknowledged (SB-FR7).
 */
final class UnitStatus {

    private boolean outOfOrder;
    private boolean announcementPending;
    private boolean repairAcknowledgementDue;

    boolean isOutOfOrder() {
        return outOfOrder;
    }

    boolean isAnnouncementPending() {
        return announcementPending;
    }

    /* A failure has been detected: the unit is out of order and the failure is announced. */
    void detectFailure() {
        if (!outOfOrder) {
            outOfOrder = true;
            announcementPending = true;
        }
    }

    /* The unit acknowledged the failure announcement; stop repeating it. */
    void acknowledgeFailure() {
        announcementPending = false;
    }

    /*
     * The unit reports itself repaired.
     *
     * @return false if the unit was not out of order, which makes the message aberrant
     */
    boolean markRepaired() {
        if (!outOfOrder) {
            return false;
        }
        outOfOrder = false;
        announcementPending = false;
        repairAcknowledgementDue = true;
        return true;
    }

    /* @return whether a repair acknowledgement is due in this cycle; clears it. */
    boolean takeRepairAcknowledgement() {
        boolean due = repairAcknowledgementDue;
        repairAcknowledgementDue = false;
        return due;
    }
}
