package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;

import java.util.List;

/**
 * Events a controller receives. {@code Gas} and {@code Obstacle} come from the
 * Vehicle; {@code Turn}, {@code Stop} and {@code Resume} are sent by the
 * gas-analysis subsystem and received by the movement subsystem.
 */
public sealed interface InputEvent {

    /** gas: one multi-sensor gas reading (CD-Evt1, CD-DM7). */
    record Gas(List<GasSensor> reading) implements InputEvent {}

    /** obstacle: the side at which an obstacle was detected (CD-Evt2). */
    record Obstacle(Loc side) implements InputEvent {}

    /** turn: the direction the robot should face next (CD-Evt4). */
    record Turn(Angle direction) implements InputEvent {}

    /** stop: the chemical source has been confirmed (CD-Evt5). */
    record Stop() implements InputEvent {}

    /** resume: the last reading showed no gas; keep searching (CD-Evt6). */
    record Resume() implements InputEvent {}
}
