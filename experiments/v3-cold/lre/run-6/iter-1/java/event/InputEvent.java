package lre.event;

import lre.annotation.RoboChartType;

/**
 * Events sent by the operator controller to the LRE (LRE-DM6).
 * Signals carry no data; reqVel and reqHdng carry a real value.
 */
public sealed interface InputEvent {

    /** Requested velocity, m/s. */
    record ReqVel(@RoboChartType("real") double value) implements InputEvent {}

    /** Requested heading, degrees. */
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
