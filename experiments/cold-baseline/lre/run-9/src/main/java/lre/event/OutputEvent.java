package lre.event;

import lre.annotation.RoboChartType;

/**
 * LRE-DM7: Output events to the autopilot controller. Only advVel and advHdng.
 */
public sealed interface OutputEvent {
    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {}
    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
