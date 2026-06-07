package chemdetector.event;

import chemdetector.data.Angle;

/**
 * Events emitted by the controllers. {@code Turn}, {@code Stop} and
 * {@code Resume} are produced by the gas-analysis subsystem for the
 * movement subsystem (CD-Evt4, CD-Evt5, CD-Evt6); {@code Flag} is
 * produced by the movement subsystem for the Vehicle (CD-Evt7).
 */
public sealed interface OutputEvent {
    record Turn(Angle direction) implements OutputEvent {}
    record Stop() implements OutputEvent {}
    record Resume() implements OutputEvent {}
    record Flag() implements OutputEvent {}
}
