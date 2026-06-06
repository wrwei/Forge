package chemdetector.event;

import chemdetector.domain.Angle;

/**
 * Inter-controller events that the gas-analysis controller emits to the
 * movement controller. These appear in the RoboChart Shared interface.
 */
public sealed interface SharedEvent {

    /** CD-Evt4 — turn ! a. */
    record Turn(Angle a) implements SharedEvent {}

    /** CD-Evt5 — stop signal. */
    record Stop() implements SharedEvent {}

    /** CD-Evt6 — resume signal. */
    record Resume() implements SharedEvent {}
}
