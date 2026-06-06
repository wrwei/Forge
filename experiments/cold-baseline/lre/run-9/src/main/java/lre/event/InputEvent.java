package lre.event;

import lre.annotation.RoboChartType;

/**
 * LRE-DM6: Input events from the operator controller.
 */
public sealed interface InputEvent {
    record ReqVel(@RoboChartType("real") double value) implements InputEvent {}
    record ReqHdng(@RoboChartType("real") double value) implements InputEvent {}
    record ReqOCM() implements InputEvent {}
    record ReqMOM() implements InputEvent {}
    record ReqHCM() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
