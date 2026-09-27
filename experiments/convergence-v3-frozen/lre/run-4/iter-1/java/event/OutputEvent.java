package lre.event;

import lre.annotation.RoboChartType;

/** The only two events the LRE sends to the autopilot controller (LRE-DM7). */
public sealed interface OutputEvent {

    /** Advised velocity in m/s. */
    record AdvVel(@RoboChartType("real") double value) implements OutputEvent {
    }

    /** Advised heading in degrees. */
    record AdvHdng(@RoboChartType("real") double value) implements OutputEvent {
    }
}
