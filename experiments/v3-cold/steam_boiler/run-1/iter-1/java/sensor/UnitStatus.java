package steam_boiler.sensor;

// What the program believes about one physical unit: whether it is out of
// order, whether the announcement of its failure still awaits acknowledgement,
// and whether a repair notification awaits the program's acknowledgement.
final class UnitStatus {
    private boolean outOfOrder = false;
    private boolean announcementPending = false;
    private boolean repairAcknowledgementDue = false;

    // A failure was detected: the unit is out of order and the detection is announced until acknowledged.
    void detectFailure() {
        outOfOrder = true;
        announcementPending = true;
    }

    void acknowledgeFailure() {
        announcementPending = false;
    }

    // The unit reported itself repaired; returns whether it had been out of order.
    boolean repair() {
        boolean wasOutOfOrder = outOfOrder;
        outOfOrder = false;
        announcementPending = false;
        repairAcknowledgementDue = true;
        return wasOutOfOrder;
    }

    boolean sound() {
        return !outOfOrder;
    }

    boolean outOfOrder() {
        return outOfOrder;
    }

    boolean announcementPending() {
        return announcementPending;
    }

    // Whether a repair acknowledgement is due this cycle; the unit repeats its repair message until it gets one.
    boolean takeRepairAcknowledgement() {
        boolean due = repairAcknowledgementDue;
        repairAcknowledgementDue = false;
        return due;
    }
}
