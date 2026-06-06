package lre.event;

/**
 * Input events from the operator controller to the LRE (LRE-DM6).
 * Data-carrying events hold a payload; signal events carry no data.
 */
public sealed interface InputEvent {

    /** Operator requests a velocity, m/s. */
    record ReqVel(double value) implements InputEvent {}

    /** Operator requests a heading, degrees. */
    record ReqHdng(double value) implements InputEvent {}

    /** Operator requests Operator Control Mode. */
    record ReqOCM() implements InputEvent {}

    /** Operator requests Main Operating Mode. */
    record ReqMOM() implements InputEvent {}

    /** Operator requests High Caution Mode. */
    record ReqHCM() implements InputEvent {}

    /** Operator indicates end of current task. */
    record EndTask() implements InputEvent {}

    /** Control-cycle tick: carries no operator data; drives autonomous transitions. */
    record Tick() implements InputEvent {}
}
