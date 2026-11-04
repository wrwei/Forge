package lre.event;

import lre.annotation.RoboChartType;

/**
 * The only two events the LRE sends to the autopilot controller (LRE-DM7).
 */
public sealed interface OutputEvent {
    /** Advised velocity (m/s). */
    record AdvVel(@RoboChartType("real") double advisedVelocity) implements OutputEvent {}
    /** Advised heading (degrees). */
    record AdvHdng(@RoboChartType("real") double advisedHeading) implements OutputEvent {}
}
