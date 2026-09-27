package sranger.event;

/** Input events the SRanger controller reacts to (SR-DM3). */
public sealed interface InputEvent {

    /** Emitted by the IR framework when the distance crosses below the threshold. */
    record Obstacle() implements InputEvent {}

    /** Delivered by the system framework once per control cycle. */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown signal. */
    record EndTask() implements InputEvent {}
}
