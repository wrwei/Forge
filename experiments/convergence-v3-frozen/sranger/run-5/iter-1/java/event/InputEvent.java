package sranger.event;

/**
 * Input events the SRanger controller consumes (SR-DM3). All three are
 * signals: they carry no payload.
 */
public sealed interface InputEvent {

    /** Emitted by the IR framework when the distance crosses below the threshold. */
    record Obstacle() implements InputEvent {}

    /** Delivered by the system framework once per control cycle. */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown request. */
    record EndTask() implements InputEvent {}
}
