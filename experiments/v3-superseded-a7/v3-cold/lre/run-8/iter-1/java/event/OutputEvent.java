package lre.event;

import lre.annotation.RoboChartType;

/**
 * Advice from the LRE to the autopilot controller; the only outputs the LRE issues.
 */
public sealed interface OutputEvent {

    /** Advised velocity (m/s). */
    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {}

    /** Advised heading (degrees). */
    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
