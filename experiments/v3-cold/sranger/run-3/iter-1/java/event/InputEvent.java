package sranger.event;

/**
 * Input events of the SRanger controller. All three are signals without a
 * payload.
 */
public sealed interface InputEvent {

    /** The IR sensor framework reports that the distance has crossed below the threshold. */
    record Obstacle() implements InputEvent {}

    /** The system framework's control-cycle tick. */
    record Tick() implements InputEvent {}

    /** The operator-level shutdown channel asks the controller to terminate. */
    record EndTask() implements InputEvent {}
}
