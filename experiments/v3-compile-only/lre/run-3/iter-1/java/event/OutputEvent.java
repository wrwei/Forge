package lre.event;

import lre.annotation.RoboChartType;

/**
 * The only events the LRE sends to the autopilot controller (LRE-DM7).
 */
public sealed interface OutputEvent {
    /** advVel: advised velocity, m/s. */
    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {}
    /** advHdng: advised heading, degrees. */
    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
