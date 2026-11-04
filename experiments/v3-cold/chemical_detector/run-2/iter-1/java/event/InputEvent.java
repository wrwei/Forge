package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Loc;
import java.util.List;

/** Events received by the controllers. */
public sealed interface InputEvent {

    /**
     * A multi-sensor gas reading from the Vehicle (CD-Evt1, CD-DM7);
     * position k of the list is the value of sensor k.
     *
     * @param readings the ordered gas-sensor values
     */
    record Gas(List<GasSample> readings) implements InputEvent {}

    /**
     * An obstacle reported by the Vehicle (CD-Evt2).
     *
     * @param side the side on which the obstacle was detected
     */
    record Obstacle(Loc side) implements InputEvent {}

    /**
     * Direction command from the gas-analysis subsystem (CD-Evt4).
     *
     * @param direction the direction to face next
     */
    record Turn(Angle direction) implements InputEvent {}

    /** The chemical source has been confirmed (CD-Evt5). */
    record Stop() implements InputEvent {}

    /** The last reading had no target chemical; keep searching (CD-Evt6). */
    record Resume() implements InputEvent {}
}
