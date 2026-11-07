package sranger.event;

/**
 * Input events of the SRanger controller (SR-DM3). All three are signals.
 */
public sealed interface InputEvent {

    /** IR sensor framework: the distance reading crossed below the obstacle threshold. */
    record Obstacle() implements InputEvent {}

    /** System framework: one control cycle has elapsed. */
    record Tick() implements InputEvent {}

    /** Operator-level shutdown channel: terminate the controller into Final. */
    record EndTask() implements InputEvent {}
}
