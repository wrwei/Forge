package lre.event;

/**
 * LRE-DM6: operator -> LRE input events.
 */
public sealed interface InputEvent {
    record ReqVel(double value) implements InputEvent {}
    record ReqHdng(double value) implements InputEvent {}
    record ReqOCM() implements InputEvent {}
    record ReqMOM() implements InputEvent {}
    record ReqHCM() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
