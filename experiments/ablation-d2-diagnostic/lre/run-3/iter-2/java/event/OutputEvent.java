package lre.event;

import lre.annotation.RoboChartType;

/** Events the LRE sends to the autopilot controller. */
public sealed interface OutputEvent {

    record advVel(@RoboChartType("real") double value) implements OutputEvent {
    }

    record advHdng(@RoboChartType("real") double value) implements OutputEvent {
    }
}
