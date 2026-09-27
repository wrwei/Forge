package lre.event;

/**
 * Input events from the operator controller to the LRE.
 *
 * <ul>
 *   <li>{@link ReqVel} — requested velocity (m/s).</li>
 *   <li>{@link ReqHdng} — requested heading (degrees).</li>
 *   <li>{@link ReqOCM} — request Operator Control Mode.</li>
 *   <li>{@link ReqMOM} — request Main Operating Mode.</li>
 *   <li>{@link ReqHCM} — request High Caution Mode.</li>
 *   <li>{@link EndTask} — end the current task.</li>
 * </ul>
 */
public sealed interface InputEvent {
    record ReqVel(@lre.annotation.RoboChartType("real") double value) implements InputEvent {}
    record ReqHdng(@lre.annotation.RoboChartType("real") double value) implements InputEvent {}
    record ReqOCM() implements InputEvent {}
    record ReqMOM() implements InputEvent {}
    record ReqHCM() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
