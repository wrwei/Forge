package lre.event;

import lre.annotation.RoboChartType;

/**
 * LRE-DM7: LRE -> autopilot output events.
 */
public sealed interface OutputEvent {

    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {}

    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
