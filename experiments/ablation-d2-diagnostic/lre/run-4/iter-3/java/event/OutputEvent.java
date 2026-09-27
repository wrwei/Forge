package lre.event;

import lre.annotation.RoboChartType;

/**
 * Events the LRE sends to the autopilot controller (LRE-DM7).
 */
public sealed interface OutputEvent {

    /** Advised velocity in m/s. */
    record advVel(@RoboChartType("real") double value) implements OutputEvent {
    }

    /** Advised heading in degrees. */
    record advHdng(@RoboChartType("real") double value) implements OutputEvent {
    }
}
