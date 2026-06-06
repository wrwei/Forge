package sranger.event;

/**
 * Input events consumed by the SRanger controller.
 *
 * <ul>
 *   <li>{@link Obstacle} — signal from the IR sensor framework when the
 *       distance reading crosses below the obstacle-detection threshold.</li>
 *   <li>{@link Tick} — signal delivered each control cycle by the system
 *       framework.</li>
 *   <li>{@link EndTask} — signal from the operator-level shutdown channel
 *       that terminates the controller into Final.</li>
 * </ul>
 */
public sealed interface InputEvent {
    record Obstacle() implements InputEvent {}
    record Tick() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
