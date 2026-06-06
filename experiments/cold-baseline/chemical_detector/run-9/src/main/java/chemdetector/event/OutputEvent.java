package chemdetector.event;

import chemdetector.datatype.Angle;

/**
 * Output events emitted by the controllers.
 * <ul>
 *   <li>turn (Angle) - emitted by gas-analysis subsystem.</li>
 *   <li>stop / resume - emitted by gas-analysis subsystem.</li>
 *   <li>flag - emitted by movement subsystem when source is found (CD-Evt7).</li>
 * </ul>
 */
public sealed interface OutputEvent {
    record TurnOut(Angle angle) implements OutputEvent {}
    record StopOut() implements OutputEvent {}
    record ResumeOut() implements OutputEvent {}
    record Flag() implements OutputEvent {}
}
