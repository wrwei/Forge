package lre.event;

/**
 * Operator controller -> LRE input events (LRE-DM6).
 *
 * <ul>
 *   <li>ReqVel  -- carries a requested velocity in m/s.</li>
 *   <li>ReqHdng -- carries a requested heading in degrees.</li>
 *   <li>ReqOCM  -- signal: request Operator Control Mode.</li>
 *   <li>ReqMOM  -- signal: request Main Operating Mode.</li>
 *   <li>ReqHCM  -- signal: request High Caution Mode.</li>
 *   <li>EndTask -- signal: end of current task.</li>
 * </ul>
 */
public sealed interface InputEvent {
    record ReqVel(double value) implements InputEvent {}
    record ReqHdng(double value) implements InputEvent {}
    record ReqOCM() implements InputEvent {}
    record ReqMOM() implements InputEvent {}
    record ReqHCM() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
