package lre.event;

import lre.annotation.RoboChartType;

/**
 * Events sent by the operator controller to the LRE (LRE-DM6).
 */
public sealed interface InputEvent {
    /** reqVel: requested velocity, m/s. */
    record ReqVel(@RoboChartType("real") double value) implements InputEvent {}
    /** reqHdng: requested heading, degrees. */
    record ReqHdng(@RoboChartType("real") double value) implements InputEvent {}
    /** reqOCM: request Operator Control Mode. */
    record ReqOCM() implements InputEvent {}
    /** reqMOM: request Main Operating Mode. */
    record ReqMOM() implements InputEvent {}
    /** reqHCM: request High Caution Mode. */
    record ReqHCM() implements InputEvent {}
    /** endTask: the current task has ended. */
    record EndTask() implements InputEvent {}
}
