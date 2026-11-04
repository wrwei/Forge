package lre.event;

import lre.annotation.RoboChartType;

/**
 * Inputs from the operator controller to the LRE: one record per event.
 */
public sealed interface InputEvent {

    /** Requested velocity, in m/s. */
    record ReqVel(@RoboChartType("real") double value) implements InputEvent {}

    /** Requested heading, in degrees. */
    record ReqHdng(@RoboChartType("real") double value) implements InputEvent {}

    /** Request for Operator Control Mode. */
    record ReqOCM() implements InputEvent {}

    /** Request for Main Operating Mode. */
    record ReqMOM() implements InputEvent {}

    /** Request for High Caution Mode. */
    record ReqHCM() implements InputEvent {}

    /** End of the current task. */
    record EndTask() implements InputEvent {}
}
