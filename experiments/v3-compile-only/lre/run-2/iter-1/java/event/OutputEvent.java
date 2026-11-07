package lre.event;

import lre.annotation.RoboChartType;

/**
 * The only outputs the LRE sends to the autopilot controller.
 */
public sealed interface OutputEvent {

    /**
     * Advised velocity.
     *
     * @param velocity the advised velocity, m/s
     */
    record AdvVel(@RoboChartType("real") double velocity) implements OutputEvent {
    }

    /**
     * Advised heading.
     *
     * @param heading the advised heading, degrees
     */
    record AdvHdng(@RoboChartType("real") double heading) implements OutputEvent {
    }
}
