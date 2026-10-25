package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events a controller emits. Turn, Stop and Resume are sent by the gas-analysis
 * subsystem to the movement subsystem; Flag is sent by the movement subsystem
 * to the Vehicle.
 */
public sealed interface OutputEvent {
    record Turn(Angle angle) implements OutputEvent {}
    record Stop() implements OutputEvent {}
    record Resume() implements OutputEvent {}
    record Flag() implements OutputEvent {}
}
