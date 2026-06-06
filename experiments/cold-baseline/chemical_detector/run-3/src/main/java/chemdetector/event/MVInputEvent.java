package chemdetector.event;

import chemdetector.data.Angle;
import chemdetector.data.Loc;

/**
 * Input events consumed by the movement controller. Obstacle and
 * Odometer are boundary inputs from the Vehicle (CD-Evt2,
 * CD-Evt3). Turn, Stop, Resume are shared events emitted by the
 * gas-analysis controller (CD-Evt4, CD-Evt5, CD-Evt6).
 */
public sealed interface MVInputEvent {

    /** CD-Evt2: obstacle detected at side l. */
    record Obstacle(Loc l) implements MVInputEvent {
    }

    /** CD-Evt3: odometer reading (cumulative distance). */
    record Odometer(double d) implements MVInputEvent {
    }

    /** CD-Evt4: turn-towards-strongest-signal command. */
    record Turn(Angle a) implements MVInputEvent {
    }

    /** CD-Evt5: stop signal (chemical source confirmed). */
    record Stop() implements MVInputEvent {
    }

    /** CD-Evt6: resume signal (no gas; continue searching). */
    record Resume() implements MVInputEvent {
    }
}
