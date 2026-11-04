package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Loc;
import java.util.List;

/**
 * Events the controllers receive: gas and obstacle from the Vehicle, and turn,
 * stop and resume from the gas-analysis subsystem to the movement subsystem.
 */
public sealed interface InputEvent {

    /** CD-Evt1: one multi-sensor gas reading (CD-DM7). */
    record Gas(List<GasSample> readings) implements InputEvent {}

    /** CD-Evt2: the side on which an obstacle has been detected. */
    record Obstacle(Loc side) implements InputEvent {}

    /** CD-Evt4: the direction the robot should face next. */
    record Turn(Angle direction) implements InputEvent {}

    /** CD-Evt5: the chemical source has been confirmed. */
    record Stop() implements InputEvent {}

    /** CD-Evt6: the last reading had no gas; continue searching. */
    record Resume() implements InputEvent {}
}
