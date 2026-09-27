package chemical_detector.event;

import chemical_detector.data.Angle;
import chemical_detector.data.Loc;

/**
 * Events consumed by the movement subsystem: the obstacle report from the
 * Vehicle (CD-Evt2) and the turn / stop / resume commands from the
 * gas-analysis subsystem (CD-Evt4, CD-Evt5, CD-Evt6).
 */
public sealed interface MovementEvent {

    record Obstacle(Loc l) implements MovementEvent {
    }

    record Turn(Angle a) implements MovementEvent {
    }

    record Stop() implements MovementEvent {
    }

    record Resume() implements MovementEvent {
    }
}
