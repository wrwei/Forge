package steam_boiler.sensor;

/**
 * Bookkeeping for one physical unit: whether it is believed out of order,
 * whether its failure detection still awaits acknowledgement, and whether a
 * repair must be acknowledged in this cycle.
 */
final class UnitStatus {

    private boolean failed;
    private boolean announcementPending;
    private boolean repairAcknowledgementDue;

    void startCycle() {
        repairAcknowledgementDue = false;
    }

    void detectFailure() {
        failed = true;
        announcementPending = true;
    }

    void acknowledgeFailure() {
        announcementPending = false;
    }

    void repair() {
        failed = false;
        announcementPending = false;
        repairAcknowledgementDue = true;
    }

    boolean failed() {
        return failed;
    }

    boolean announcementPending() {
        return announcementPending;
    }

    boolean repairAcknowledgementDue() {
        return repairAcknowledgementDue;
    }
}
