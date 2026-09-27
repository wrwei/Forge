package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events emitted by a controller. {@code Turn}, {@code Stop} and {@code Resume} are
 * sent by the gas-analysis subsystem to the movement subsystem; {@code Flag} is sent by
 * the movement subsystem to the Vehicle.
 */
public sealed interface OutputEvent {
    record Turn(Angle a) implements OutputEvent {}
    record Stop() implements OutputEvent {}
    record Resume() implements OutputEvent {}
    record Flag() implements OutputEvent {}
}
