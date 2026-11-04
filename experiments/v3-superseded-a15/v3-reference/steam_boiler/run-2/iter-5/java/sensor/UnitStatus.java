package steam_boiler.sensor;

/**
 * What the program believes about one physical unit: sound or out of order, and whether the
 * failure detection announced for it has been acknowledged yet.
 */
final class UnitStatus {
    private boolean outOfOrder = false;
    private boolean detectionAcknowledged = true;

    boolean sound() {
        return !outOfOrder;
    }

    /** True while the failure detection must still be announced on every cycle. */
    boolean announcementPending() {
        return outOfOrder && !detectionAcknowledged;
    }

    void markFailed() {
        outOfOrder = true;
        detectionAcknowledged = false;
    }

    /** Records a failure acknowledgement; false if no announcement was pending (an aberrant message). */
    boolean acknowledgeDetection() {
        if (!announcementPending()) {
            return false;
        }
        detectionAcknowledged = true;
        return true;
    }

    /** Records a repair notification; false if the unit was not out of order (an aberrant message). */
    boolean markRepaired() {
        if (!outOfOrder) {
            return false;
        }
        outOfOrder = false;
        detectionAcknowledged = true;
        return true;
    }
}
