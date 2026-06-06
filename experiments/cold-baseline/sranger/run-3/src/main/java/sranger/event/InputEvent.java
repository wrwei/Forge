package sranger.event;

/**
 * Input events for the SRanger controller (SR-DM3).
 * - obstacle: emitted by the IR sensor framework when distance crosses below the threshold
 * - tick: delivered each control cycle by the framework
 * - endTask: operator-level shutdown signal
 */
public sealed interface InputEvent {
    record Obstacle() implements InputEvent {}
    record Tick() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
