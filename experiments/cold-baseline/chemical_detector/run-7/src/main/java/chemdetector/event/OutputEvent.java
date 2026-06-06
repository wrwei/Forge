package chemdetector.event;

import chemdetector.domain.Angle;

/**
 * Sealed-interface enumeration of every event the controllers can
 * emit toward the Vehicle / environment, or toward one another via
 * the actuator.
 *
 * Boundary outputs (controllers -> Vehicle):
 *   - Flag (CD-Evt7)
 *
 * Inter-controller outputs (carried through the actuator):
 *   - Turn   (CD-Evt4)
 *   - Stop   (CD-Evt5)
 *   - Resume (CD-Evt6)
 */
public sealed interface OutputEvent {

    /** CD-Evt7 — chemical source located, halt the platform. */
    record Flag() implements OutputEvent {
    }

    /** CD-Evt4 — direction command emitted by gas analysis. */
    record Turn(Angle a) implements OutputEvent {
    }

    /** CD-Evt5 — gas analysis tells movement to halt. */
    record Stop() implements OutputEvent {
    }

    /** CD-Evt6 — gas analysis tells movement to resume search. */
    record Resume() implements OutputEvent {
    }
}
