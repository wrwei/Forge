package chemical_detector.event;

import chemical_detector.data.Angle;
import chemical_detector.data.GasSample;
import chemical_detector.data.Loc;
import java.util.List;

/**
 * Events a controller receives. {@code Gas} and {@code Obstacle} come from the
 * Vehicle; {@code Turn}, {@code Stop} and {@code Resume} are the commands the
 * gas-analysis subsystem sends to the movement subsystem.
 */
public sealed interface InputEvent {

    /** One multi-sensor gas reading. */
    record Gas(List<GasSample> readings) implements InputEvent {
    }

    /** An obstacle detected on the given side of the robot. */
    record Obstacle(Loc side) implements InputEvent {
    }

    /** Command to face the given direction. */
    record Turn(Angle direction) implements InputEvent {
    }

    /** The chemical source has been confirmed. */
    record Stop() implements InputEvent {
    }

    /** The last reading showed no gas; keep searching. */
    record Resume() implements InputEvent {
    }
}
