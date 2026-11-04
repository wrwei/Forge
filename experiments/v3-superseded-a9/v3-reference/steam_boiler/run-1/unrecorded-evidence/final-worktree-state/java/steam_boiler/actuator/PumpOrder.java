package steam_boiler.actuator;

import steam_boiler.annotation.RoboChartType;

/** The last order sent to one pump and how many transmissions have been received since. */
final class PumpOrder {

    /** Ages are counted up to this value; older orders behave alike. */
    @RoboChartType("nat") static final int SETTLED_AGE = 2;

    private boolean orderedOn;
    @RoboChartType("nat") private int transmissionsSinceOrder = SETTLED_AGE;

    boolean orderedOn() {
        return orderedOn;
    }

    @RoboChartType("nat")
    int transmissionsSinceOrder() {
        return transmissionsSinceOrder;
    }

    void issue(boolean on) {
        this.orderedOn = on;
        this.transmissionsSinceOrder = 0;
    }

    void advance() {
        if (transmissionsSinceOrder < SETTLED_AGE) {
            transmissionsSinceOrder = transmissionsSinceOrder + 1;
        }
    }
}
