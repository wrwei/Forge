package sranger.event;

/**
 * Input events of the SRanger controller. All three are signals without a
 * payload.
 */
public sealed interface InputEvent {

    /** The IR distance reading has crossed below the obstacle threshold. */
    record Obstacle() implements InputEvent {}

    /** One control cycle has elapsed. */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown: terminate the controller into Final. */
    record EndTask() implements InputEvent {}
}
