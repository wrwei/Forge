package lre.event;

import lre.annotation.RoboChartType;

/**
 * The only two outputs the LRE issues to the autopilot controller (LRE-DM7).
 */
public sealed interface OutputEvent {

    /** Advised velocity, m/s. */
    record advVel(@RoboChartType("real") double value) implements OutputEvent {
    }

    /** Advised heading, degrees. */
    record advHdng(@RoboChartType("real") double value) implements OutputEvent {
    }
}
