package chemdetector.event;

import chemdetector.data.Angle;
import chemdetector.data.Loc;

/**
 * Inputs received by the movement controller's step() method:
 *   - shared events from the gas-analysis controller (turn, stop, resume),
 *   - vehicle-sensor events (obstacle, odometer).
 */
public sealed interface MVInputEvent {

    /** CD-Evt4 — turn event carrying an Angle (shared GA -> MV). */
    record Turn(Angle a) implements MVInputEvent {
    }

    /** CD-Evt5 — stop signal (shared GA -> MV). */
    record Stop() implements MVInputEvent {
    }

    /** CD-Evt6 — resume signal (shared GA -> MV). */
    record Resume() implements MVInputEvent {
    }

    /** CD-Evt2 — obstacle event carrying a Loc (Vehicle -> MV). */
    record Obstacle(Loc l) implements MVInputEvent {
    }

    /** CD-Evt3 — odometer event carrying the current distance (Vehicle -> MV). */
    record Odometer(double d) implements MVInputEvent {
    }
}
