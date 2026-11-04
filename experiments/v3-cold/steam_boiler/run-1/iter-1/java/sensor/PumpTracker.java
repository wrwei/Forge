package steam_boiler.sensor;

import steam_boiler.annotation.RoboChartType;

// One pump and its controller: the state the program has ordered, the latest
// reports, failure judgement, and whether the pump pours water.
final class PumpTracker {
    // The controller is judged on the second transmission after an order that changes the pump's state.
    @RoboChartType("nat") private static final int SETTLED = 2;

    private final UnitStatus pumpUnit = new UnitStatus();
    private final UnitStatus controllerUnit = new UnitStatus();
    private boolean commandedOn = false;
    @RoboChartType("nat") private int cyclesSinceChange = SETTLED;
    private boolean reportedOn = false;
    private boolean flowReported = false;
    private boolean knownDelivering = false;
    private boolean possiblyDelivering = false;
    private boolean elapsedKnown = false;
    private boolean elapsedPossible = false;

    // Accounts for the orders sent to this pump in the previous cycle. Stopping takes
    // effect at once; a started pump pours nothing during the cycle after its order.
    void applyOrders(boolean opened, boolean closed) {
        elapsedKnown = knownDelivering && !closed;
        elapsedPossible = possiblyDelivering && !(closed && pumpUnit.sound());
        boolean target = commandedOn;
        if (opened) {
            target = true;
        } else if (closed) {
            target = false;
        }
        if (target != commandedOn) {
            cyclesSinceChange = 1;
        } else if (cyclesSinceChange < SETTLED) {
            cyclesSinceChange = cyclesSinceChange + 1;
        }
        commandedOn = target;
    }

    void receive(TransmissionSensor in, @RoboChartType("nat") int pump) {
        reportedOn = in.pumpReportedOn(pump);
        flowReported = in.flowReported(pump);
        if (in.pumpFailureAckReceived(pump)) {
            pumpUnit.acknowledgeFailure();
        }
        if (in.pumpControlFailureAckReceived(pump)) {
            controllerUnit.acknowledgeFailure();
        }
        if (in.pumpRepairReported(pump) && pumpUnit.repair()) {
            // Resume from the state the repaired pump reports.
            commandedOn = reportedOn;
            cyclesSinceChange = 1;
        }
        if (in.pumpControlRepairReported(pump)) {
            controllerUnit.repair();
        }
    }

    void detect() {
        // The pump must be in the state last ordered: it failed to obey, or changed spontaneously.
        if (pumpUnit.sound() && reportedOn != commandedOn) {
            pumpUnit.detectFailure();
        }
        // Once settled, a sound pump's controller must report flow exactly when the pump is on.
        if (controllerUnit.sound() && pumpUnit.sound() && cyclesSinceChange >= SETTLED
                && flowReported != commandedOn) {
            controllerUnit.detectFailure();
        }
    }

    void settle() {
        if (controllerUnit.sound()) {
            knownDelivering = flowReported;
        } else {
            knownDelivering = pumpUnit.sound() && reportedOn && cyclesSinceChange >= SETTLED;
        }
        possiblyDelivering = flowReported || reportedOn || pumpUnit.outOfOrder() || controllerUnit.outOfOrder();
    }

    UnitStatus pumpUnit() {
        return pumpUnit;
    }

    UnitStatus controllerUnit() {
        return controllerUnit;
    }

    boolean elapsedKnown() {
        return elapsedKnown;
    }

    boolean elapsedPossible() {
        return elapsedPossible;
    }

    boolean knownDelivering() {
        return knownDelivering;
    }

    boolean possiblyDelivering() {
        return possiblyDelivering;
    }
}
