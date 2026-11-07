package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events emitted by the reasoning subsystems. Turn, Stop and Resume are sent by
 * the gas-analysis subsystem to the movement subsystem; Flag is sent by the
 * movement subsystem to the Vehicle.
 */
public sealed interface OutputEvent {
    /** Steer towards the strongest signal (CD-Evt4). */
    record Turn(Angle direction) implements OutputEvent {}

    /** The chemical source has been confirmed (CD-Evt5). */
    record Stop() implements OutputEvent {}

    /** A no-gas reading was analysed; continue searching (CD-Evt6). */
    record Resume() implements OutputEvent {}

    /** The chemical source has been located (CD-Evt7). */
    record Flag() implements OutputEvent {}
}
