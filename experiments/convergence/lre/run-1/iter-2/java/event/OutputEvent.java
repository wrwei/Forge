package lre.event;

import lre.annotation.RoboChartType;

/**
 * Output events from the LRE to the autopilot controller (LRE-DM7).
 * advVel and advHdng are the only two outputs the LRE issues.
 */
public sealed interface OutputEvent {

    /** Advised velocity, m/s. */
    record advVel(@RoboChartType("real") double value) implements OutputEvent {
    }

    /** Advised heading, degrees. */
    record advHdng(@RoboChartType("real") double value) implements OutputEvent {
    }
}
