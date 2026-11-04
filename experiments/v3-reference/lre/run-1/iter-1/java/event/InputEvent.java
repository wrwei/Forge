package lre.event;

import lre.annotation.RoboChartType;

/**
 * Inputs the operator controller sends to the LRE (LRE-DM6).
 * Velocity and heading requests carry a value; the others are signals.
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
