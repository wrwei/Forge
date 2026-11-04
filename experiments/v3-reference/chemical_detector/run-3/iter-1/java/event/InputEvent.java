package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;
import java.util.List;

/** Events a controller receives: one record per event. */
public sealed interface InputEvent {

    /**
     * A multi-sensor gas reading from the Vehicle (CD-Evt1), received by the
     * gas-analysis subsystem.
     *
     * @param samples the ordered reading; position (1-based) encodes the sensing direction
     */
    record Gas(List<GasSensor> samples) implements InputEvent {}

    /**
     * An obstacle detected by the Vehicle (CD-Evt2), received by the movement
     * subsystem.
     *
     * @param side the side of the robot on which the obstacle was detected
     */
    record Obstacle(Loc side) implements InputEvent {}

    /**
     * The direction to face next, sent by the gas-analysis subsystem and
     * received by the movement subsystem (CD-Evt4).
     *
     * @param direction the commanded direction
     */
    record Turn(Angle direction) implements InputEvent {}

    /** The chemical source is confirmed; received by the movement subsystem (CD-Evt5). */
    record Stop() implements InputEvent {}

    /** A no-gas reading was analysed; received by the movement subsystem (CD-Evt6). */
    record Resume() implements InputEvent {}
}
