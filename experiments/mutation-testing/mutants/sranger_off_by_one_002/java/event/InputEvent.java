package sranger.event;

/**
 * Input events received by the SRanger controller (SR-DM3).
 *
 * <p>All three input events are signal events (no payload):</p>
 * <ul>
 *   <li>{@link Obstacle} — IR sensor framework reports the distance has crossed below
 *       the obstacle-detection threshold.</li>
 *   <li>{@link Tick} — system framework delivers one tick per control cycle.</li>
 *   <li>{@link EndTask} — operator-level shutdown channel terminates the controller.</li>
 * </ul>
 */
public sealed interface InputEvent {
    record Obstacle() implements InputEvent {}
    record Tick() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
