package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events a controller emits. The gas-analysis subsystem emits {@code turn}, {@code stop}
 * and {@code resume} (CD-Evt4..6); the movement subsystem emits {@code flag} to the
 * Vehicle when the chemical source is found (CD-Evt7).
 */
public sealed interface OutputEvent {
    record turn(Angle direction) implements OutputEvent {}
    record stop() implements OutputEvent {}
    record resume() implements OutputEvent {}
    record flag() implements OutputEvent {}
}
