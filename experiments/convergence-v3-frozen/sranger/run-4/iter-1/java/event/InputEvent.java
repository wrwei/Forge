package sranger.event;

/** Input events accepted by the SRanger controller (SR-DM3). All are signals. */
public sealed interface InputEvent {

    /** Raised by the IR framework when the distance reading crosses below the threshold. */
    record Obstacle() implements InputEvent {}

    /** Delivered by the system framework once per control cycle. */
    record Tick() implements InputEvent {}

    /** Raised by the operator-level shutdown channel. */
    record EndTask() implements InputEvent {}
}
