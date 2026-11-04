package lre.event;

import lre.annotation.RoboChartType;

/**
 * Events sent by the operator controller to the LRE.
 */
public sealed interface InputEvent {

    /** Requested velocity (m/s). */
    record ReqVel(@RoboChartType("real") double requestedVelocity) implements InputEvent {}

    /** Requested heading (degrees). */
    record ReqHdng(@RoboChartType("real") double requestedHeading) implements InputEvent {}

    /** Request for Operator Control Mode. */
    record ReqOCM() implements InputEvent {}

    /** Request for Main Operating Mode. */
    record ReqMOM() implements InputEvent {}

    /** Request for High Caution Mode. */
    record ReqHCM() implements InputEvent {}

    /** End of the current task. */
    record EndTask() implements InputEvent {}
}
