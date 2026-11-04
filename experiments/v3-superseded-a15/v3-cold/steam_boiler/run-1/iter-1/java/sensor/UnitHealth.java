package steam_boiler.sensor;

/**
 * Bookkeeping for one physical unit (SB-V9): whether it is believed out of
 * order, whether its failure detection still awaits acknowledgement, and what
 * changed in the current cycle.
 */
final class UnitHealth {
    private boolean outOfOrder;
    private boolean detectionPending;
    private boolean failedThisCycle;
    private boolean repairedThisCycle;

    void startCycle() {
        failedThisCycle = false;
        repairedThisCycle = false;
    }

    void judgeFailed() {
        outOfOrder = true;
        detectionPending = true;
        failedThisCycle = true;
    }

    void markRepaired() {
        outOfOrder = false;
        detectionPending = false;
        repairedThisCycle = true;
    }

    void acknowledgeDetection() {
        detectionPending = false;
    }

    boolean outOfOrder() {
        return outOfOrder;
    }

    boolean detectionPending() {
        return detectionPending;
    }

    boolean failedThisCycle() {
        return failedThisCycle;
    }

    boolean repairedThisCycle() {
        return repairedThisCycle;
    }
}
