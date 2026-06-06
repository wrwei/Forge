package sranger.event;

/**
 * Input events accepted by the SRanger controller (SR-DM3).
 * All three input events are signals — no payload.
 */
public sealed interface InputEvent {
    record Obstacle() implements InputEvent {}
    record Tick() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
