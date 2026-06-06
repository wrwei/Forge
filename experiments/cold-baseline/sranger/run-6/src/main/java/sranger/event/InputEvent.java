package sranger.event;

/**
 * SR-DM3: Input events delivered to the SRanger controller.
 * - obstacle: IR sensor framework signals distance below threshold.
 * - tick: control-cycle delivery from the system framework.
 * - endTask: operator-level shutdown.
 */
public sealed interface InputEvent {
    record obstacle() implements InputEvent {}
    record tick() implements InputEvent {}
    record endTask() implements InputEvent {}
}
