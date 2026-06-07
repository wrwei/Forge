package lre.event;

import lre.annotation.RoboChartType;

/**
 * Output events from the LRE controller to the autopilot controller.
 * These are the only two outputs the LRE issues.
 */
public sealed interface OutputEvent {

    /** Advised velocity in m/s. */
    record advVel(@RoboChartType("real") double value) implements OutputEvent {}

    /** Advised heading in degrees. */
    record advHdng(@RoboChartType("real") double value) implements OutputEvent {}
}
