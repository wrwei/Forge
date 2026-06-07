package lre.event;

import lre.annotation.RoboChartType;

/**
 * Output events issued by the LRE to the autopilot controller. These are
 * the only two outputs the LRE issues.
 */
public sealed interface OutputEvent {

    /** Advised velocity, m/s. */
    record advVel(@RoboChartType("real") double value) implements OutputEvent {}

    /** Advised heading, degrees. */
    record advHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
