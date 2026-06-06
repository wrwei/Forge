package lre.event;

import lre.annotation.RoboChartType;

/**
 * Output events from the LRE to the autopilot controller (LRE-DM7).
 * The LRE has ONLY two output events: AdvVel and AdvHdng.
 */
public sealed interface OutputEvent {

    /** Advised velocity in m/s. */
    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {}

    /** Advised heading in degrees. */
    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
