package chemical_detector.event;

import chemical_detector.Angle;
import chemical_detector.GasSensor;
import chemical_detector.Loc;
import java.util.List;

/**
 * Events received by the controllers: gas and obstacle from the Vehicle, and turn,
 * stop and resume from the gas-analysis subsystem to the movement subsystem.
 */
public sealed interface InputEvent {

    /** CD-Evt1: one multi-sensor gas reading, ordered by sensing direction (CD-DM7). */
    record Gas(List<GasSensor> reading) implements InputEvent {
    }

    /** CD-Evt2: an obstacle has been detected on the given side. */
    record Obstacle(Loc side) implements InputEvent {
    }

    /** CD-Evt4: the direction the robot should face next. */
    record Turn(Angle direction) implements InputEvent {
    }

    /** CD-Evt5: the chemical source has been confirmed. */
    record Stop() implements InputEvent {
    }

    /** CD-Evt6: the last reading showed no gas; continue searching. */
    record Resume() implements InputEvent {
    }
}
