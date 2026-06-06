package lre.event;

import lre.annotation.RoboChartType;

/**
 * Output events from the LRE to the autopilot controller (LRE-DM7).
 */
public sealed interface OutputEvent {

    record advVel(@RoboChartType("real") double value) implements OutputEvent {
    }

    record advHdng(@RoboChartType("real") double value) implements OutputEvent {
    }
}
