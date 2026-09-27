package lre.event;

import lre.annotation.RoboChartType;

/** The only outputs the LRE issues to the autopilot controller. */
public sealed interface OutputEvent {

    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {
    }

    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {
    }
}
