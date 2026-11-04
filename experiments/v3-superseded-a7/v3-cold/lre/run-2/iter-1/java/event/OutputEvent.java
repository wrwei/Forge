package lre.event;

import lre.annotation.RoboChartType;

/**
 * Events from the LRE to the autopilot controller; the only outputs of the LRE (LRE-DM7).
 */
public sealed interface OutputEvent {

    /** Advised velocity, in m/s. */
    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {}

    /** Advised heading, in degrees. */
    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
