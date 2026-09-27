package sranger.event;

/** Input events delivered to the SRanger controller. */
public sealed interface InputEvent {
    /** IR distance crossed below the obstacle threshold. */
    record Obstacle() implements InputEvent {}
    /** One control cycle elapsed. */
    record Tick() implements InputEvent {}
    /** Operator-level shutdown. */
    record EndTask() implements InputEvent {}
}
