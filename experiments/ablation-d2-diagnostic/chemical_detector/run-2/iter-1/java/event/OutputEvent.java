package chemical_detector.event;

import chemical_detector.data.Angle;

/**
 * Events a controller emits. {@code turn}, {@code stop} and {@code resume} are
 * emitted by the gas-analysis subsystem for the movement subsystem;
 * {@code flag} is emitted by the movement subsystem to the Vehicle.
 */
public sealed interface OutputEvent {
    record turn(Angle direction) implements OutputEvent {}
    record stop() implements OutputEvent {}
    record resume() implements OutputEvent {}
    record flag() implements OutputEvent {}
}
