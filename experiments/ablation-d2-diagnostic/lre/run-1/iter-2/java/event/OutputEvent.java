package lre.event;

import lre.annotation.RoboChartType;

/**
 * The only two events the LRE sends to the autopilot controller (LRE-DM7).
 */
public sealed interface OutputEvent {

    /** Advises a velocity of {@code value} m/s. */
    record advVel(@RoboChartType("real") double value) implements OutputEvent {
    }

    /** Advises a heading of {@code value} degrees. */
    record advHdng(@RoboChartType("real") double value) implements OutputEvent {
    }
}
