package lre.event;

/**
 * Input events received by the LRE from the operator controller (LRE-DM6).
 */
public sealed interface InputEvent {

    /** Operator requests a velocity (m/s). */
    record reqVel(double value) implements InputEvent {}

    /** Operator requests a heading (degrees). */
    record reqHdng(double value) implements InputEvent {}

    /** Operator requests Operator Control Mode. */
    record reqOCM() implements InputEvent {}

    /** Operator requests Main Operating Mode. */
    record reqMOM() implements InputEvent {}

    /** Operator requests High Caution Mode. */
    record reqHCM() implements InputEvent {}

    /** Operator signals end of the current task. */
    record endTask() implements InputEvent {}
}
