package lre.event;

import lre.annotation.RoboChartType;

/**
 * Advice from the LRE to the autopilot controller (LRE-DM7). These are the
 * only two outputs the LRE issues.
 */
public sealed interface OutputEvent {

    /** Advised velocity in m/s. */
    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {}

    /** Advised heading in degrees. */
    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
