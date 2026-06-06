package chemdetector.gasanalysis.event;

import chemdetector.data.Angle;

/**
 * Events emitted by the gas-analysis subsystem to the movement
 * subsystem. These are SHARED events (CD-Evt4, CD-Evt5, CD-Evt6).
 */
public sealed interface OutputEvent {

    /** CD-Evt4: send the new heading to the movement subsystem. */
    record Turn(Angle a) implements OutputEvent {}

    /** CD-Evt5: tell the movement subsystem the source has been found. */
    record Stop() implements OutputEvent {}

    /** CD-Evt6: tell the movement subsystem to resume searching. */
    record Resume() implements OutputEvent {}
}
