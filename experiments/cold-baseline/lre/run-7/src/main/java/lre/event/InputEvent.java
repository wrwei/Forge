package lre.event;

import lre.annotation.RoboChartType;

/**
 * Input events from the operator controller to the LRE (LRE-DM6).
 */
public sealed interface InputEvent {

    /** Operator-requested velocity (m/s). */
    record ReqVel(@RoboChartType("real") double value) implements InputEvent {}

    /** Operator-requested heading (degrees). */
    record ReqHdng(@RoboChartType("real") double value) implements InputEvent {}

    /** Operator requests Operator Control Mode. */
    record ReqOCM() implements InputEvent {}

    /** Operator requests Main Operating Mode. */
    record ReqMOM() implements InputEvent {}

    /** Operator requests High Caution Mode. */
    record ReqHCM() implements InputEvent {}

    /** End of current task. */
    record EndTask() implements InputEvent {}
}
