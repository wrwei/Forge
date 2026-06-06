package chemdetector.event;

import chemdetector.data.Angle;

/**
 * Shared inter-controller events emitted by the gas-analysis controller
 * and consumed by the movement controller.
 */
public sealed interface SharedEvent {

    /** CD-Evt4 — turn ! Angle. */
    record Turn(Angle a) implements SharedEvent {
    }

    /** CD-Evt5 — stop (signal). */
    record Stop() implements SharedEvent {
    }

    /** CD-Evt6 — resume (signal). */
    record Resume() implements SharedEvent {
    }
}
