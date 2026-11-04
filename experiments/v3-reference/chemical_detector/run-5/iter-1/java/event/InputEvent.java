package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;
import java.util.List;

/**
 * Events received by the controllers: gas and obstacle from the Vehicle,
 * turn, stop and resume from the gas-analysis subsystem to the movement subsystem.
 */
public sealed interface InputEvent {

    /**
     * One multi-sensor gas reading; the 1-based position of a value encodes its sensing direction.
     *
     * @param readings the ordered gas-sensor values captured at one point in time
     */
    record Gas(List<GasSensor> readings) implements InputEvent {
    }

    /**
     * An obstacle has been detected.
     *
     * @param side the side of the robot on which the obstacle was detected
     */
    record Obstacle(Loc side) implements InputEvent {
    }

    /**
     * The direction the robot should face next.
     *
     * @param direction the commanded direction
     */
    record Turn(Angle direction) implements InputEvent {
    }

    /** The chemical source has been confirmed. */
    record Stop() implements InputEvent {
    }

    /** The latest reading showed no target chemical: return to searching. */
    record Resume() implements InputEvent {
    }
}
