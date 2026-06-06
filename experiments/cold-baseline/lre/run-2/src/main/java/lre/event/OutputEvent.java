package lre.event;

import lre.annotation.RoboChartType;

/**
 * LRE -&gt; autopilot output events (LRE-DM7).
 * <p>
 * These are the only two output events; both carry a real payload.
 */
public sealed interface OutputEvent {

    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {}

    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
