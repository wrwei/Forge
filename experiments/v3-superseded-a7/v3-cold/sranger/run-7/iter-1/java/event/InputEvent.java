package sranger.event;

/**
 * Input events delivered to the SRanger controller. All three are signals without payload.
 */
public sealed interface InputEvent {

    /** The IR distance reading has crossed below the obstacle-detection threshold. */
    record Obstacle() implements InputEvent {}

    /** One control cycle has elapsed. */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown request; terminates the controller into Final. */
    record EndTask() implements InputEvent {}
}
