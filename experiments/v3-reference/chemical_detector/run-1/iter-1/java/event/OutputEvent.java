package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events emitted by the controllers: turn, stop and resume by the
 * gas-analysis subsystem, flag by the movement subsystem.
 */
public sealed interface OutputEvent {
    record Turn(Angle direction) implements OutputEvent {}
    record Stop() implements OutputEvent {}
    record Resume() implements OutputEvent {}
    record Flag() implements OutputEvent {}
}
