package chemical_detector.event;

import chemical_detector.data.Angle;

/**
 * Events a controller emits. The gas-analysis subsystem emits {@code Turn},
 * {@code Stop} and {@code Resume} to the movement subsystem; the movement
 * subsystem emits {@code Flag} to the Vehicle.
 */
public sealed interface OutputEvent {

    /** Command to face the given direction. */
    record Turn(Angle direction) implements OutputEvent {
    }

    /** The chemical source has been confirmed. */
    record Stop() implements OutputEvent {
    }

    /** The last reading showed no gas; keep searching. */
    record Resume() implements OutputEvent {
    }

    /** Signals the Vehicle that the chemical source has been located. */
    record Flag() implements OutputEvent {
    }
}
