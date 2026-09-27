package chemical_detector.event;

import chemical_detector.types.Angle;

/**
 * Events a controller emits. {@code turn}, {@code stop} and {@code resume} are
 * emitted by the gas-analysis controller for the movement controller;
 * {@code flag} is emitted by the movement controller for the Vehicle.
 */
public sealed interface OutputEvent {

    /** Head in direction {@code a}, the direction of the strongest signal. */
    record turn(Angle a) implements OutputEvent {
    }

    /** The chemical source has been confirmed. */
    record stop() implements OutputEvent {
    }

    /** The last reading showed no target chemical; keep searching. */
    record resume() implements OutputEvent {
    }

    /** The chemical source has been located. */
    record flag() implements OutputEvent {
    }
}
