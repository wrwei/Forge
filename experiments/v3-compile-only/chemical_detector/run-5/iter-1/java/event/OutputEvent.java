package chemical_detector.event;

import chemical_detector.types.Angle;

/**
 * Events a controller sends. Turn, Stop and Resume go from the gas-analysis
 * subsystem to the movement subsystem; Flag goes from the movement subsystem
 * to the Vehicle.
 */
public sealed interface OutputEvent {

    /** Face the direction of the strongest signal (CD-Evt4). */
    record Turn(Angle direction) implements OutputEvent {}

    /** The chemical source has been confirmed (CD-Evt5). */
    record Stop() implements OutputEvent {}

    /** No gas in the last reading; continue searching (CD-Evt6). */
    record Resume() implements OutputEvent {}

    /** The chemical source has been located (CD-Evt7). */
    record Flag() implements OutputEvent {}
}
