package lre.event;

import lre.annotation.RoboChartType;

/**
 * Output events from the LRE to the autopilot controller (LRE-DM7).
 * Only two outputs exist: advVel (velocity advisory) and advHdng (heading advisory).
 */
public sealed interface OutputEvent {
    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {}
    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
