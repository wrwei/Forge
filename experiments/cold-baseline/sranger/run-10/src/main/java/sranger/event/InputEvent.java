package sranger.event;

/**
 * Input events accepted by the SRanger controller.
 * SR-DM3: obstacle (IR), tick (system framework), endTask (operator shutdown).
 */
public sealed interface InputEvent {
    record Obstacle() implements InputEvent {}
    record Tick() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
