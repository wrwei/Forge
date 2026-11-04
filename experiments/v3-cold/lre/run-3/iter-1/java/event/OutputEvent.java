package lre.event;

import lre.annotation.RoboChartType;

/** Advice from the LRE to the autopilot controller; the LRE's only outputs. */
public sealed interface OutputEvent {

    /** Advised velocity (m/s). */
    record AdvVel(@RoboChartType("real") double velocity) implements OutputEvent {}

    /** Advised heading (degrees). */
    record AdvHdng(@RoboChartType("real") double heading) implements OutputEvent {}
}
