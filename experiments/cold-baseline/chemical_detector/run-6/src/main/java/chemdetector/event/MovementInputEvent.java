package chemdetector.event;

import chemdetector.domain.Angle;
import chemdetector.domain.Loc;

/**
 * Inputs received by the movement controller. Includes Vehicle-emitted
 * obstacle and odometer events plus the shared turn/stop/resume events
 * issued by the gas-analysis controller.
 */
public sealed interface MovementInputEvent {

    /** CD-Evt2 — obstacle detected on side {@code l}. */
    record Obstacle(Loc l) implements MovementInputEvent {}

    /** CD-Evt3 — current cumulative distance travelled. */
    record Odometer(double d) implements MovementInputEvent {}

    /** CD-Evt4 — turn towards angle {@code a}. */
    record Turn(Angle a) implements MovementInputEvent {}

    /** CD-Evt5 — chemical source confirmed; halt. */
    record Stop() implements MovementInputEvent {}

    /** CD-Evt6 — resume searching after a no-gas reading. */
    record Resume() implements MovementInputEvent {}
}
