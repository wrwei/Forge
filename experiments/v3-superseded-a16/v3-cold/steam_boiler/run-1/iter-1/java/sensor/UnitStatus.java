package steam_boiler.sensor;

/**
 * What the program believes about one physical unit: sound or out of order, and whether
 * the announcement of its failure still awaits acknowledgement.
 */
final class UnitStatus {
    private boolean failed;
    private boolean awaitingAcknowledgement;
    private boolean repairedThisCycle;

    void beginCycle() {
        repairedThisCycle = false;
    }

    /** The program has detected a failure: the unit is out of order and the failure is to be announced. */
    void detectFailure() {
        failed = true;
        awaitingAcknowledgement = true;
    }

    /** Returns false if no failure announcement was awaiting acknowledgement (an aberrant message). */
    boolean acknowledgeFailure() {
        if (!awaitingAcknowledgement) {
            return false;
        }
        awaitingAcknowledgement = false;
        return true;
    }

    /** Returns false if the unit was not out of order (an aberrant message). */
    boolean repair() {
        if (!failed) {
            return false;
        }
        failed = false;
        awaitingAcknowledgement = false;
        repairedThisCycle = true;
        return true;
    }

    boolean isFailed() {
        return failed;
    }

    boolean isAwaitingAcknowledgement() {
        return awaitingAcknowledgement;
    }

    boolean wasRepairedThisCycle() {
        return repairedThisCycle;
    }

    /** Sound, and not repaired in this cycle (so its previous reports were trusted too). */
    boolean isReliable() {
        return !failed && !repairedThisCycle;
    }
}
