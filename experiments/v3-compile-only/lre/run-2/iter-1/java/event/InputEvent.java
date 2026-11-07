package lre.event;

import lre.annotation.RoboChartType;

/**
 * Inputs the operator controller sends to the LRE.
 */
public sealed interface InputEvent {

    /**
     * Requested velocity.
     *
     * @param velocity the requested velocity, m/s
     */
    record ReqVel(@RoboChartType("real") double velocity) implements InputEvent {
    }

    /**
     * Requested heading.
     *
     * @param heading the requested heading, degrees
     */
    record ReqHdng(@RoboChartType("real") double heading) implements InputEvent {
    }

    /** Request for Operator Control Mode. */
    record ReqOCM() implements InputEvent {
    }

    /** Request for Main Operating Mode. */
    record ReqMOM() implements InputEvent {
    }

    /** Request for High Caution Mode. */
    record ReqHCM() implements InputEvent {
    }

    /** End of the current task. */
    record EndTask() implements InputEvent {
    }
}
