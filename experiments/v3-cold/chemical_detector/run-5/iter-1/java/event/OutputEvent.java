package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events a controller sends. The gas-analysis subsystem sends {@code Turn},
 * {@code Stop} and {@code Resume} to the movement subsystem; the movement
 * subsystem sends {@code Flag} to the Vehicle.
 */
public sealed interface OutputEvent {
    /** Direction of the strongest signal (CD-Evt4). */
    record Turn(Angle direction) implements OutputEvent {}

    /** The chemical source has been confirmed (CD-Evt5). */
    record Stop() implements OutputEvent {}

    /** The last reading had no target chemical (CD-Evt6). */
    record Resume() implements OutputEvent {}

    /** The chemical source has been located (CD-Evt7). */
    record Flag() implements OutputEvent {}
}
