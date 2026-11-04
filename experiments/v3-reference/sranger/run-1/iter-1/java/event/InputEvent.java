package sranger.event;

/**
 * Input events delivered to the SRanger controller, one record per event.
 * All three are signals without a payload.
 */
public sealed interface InputEvent {

    /** The IR distance reading has crossed below the obstacle-detection threshold. */
    record Obstacle() implements InputEvent {}

    /** One control cycle has elapsed (delivered by the system framework). */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown: terminates the controller into Final. */
    record EndTask() implements InputEvent {}
}
