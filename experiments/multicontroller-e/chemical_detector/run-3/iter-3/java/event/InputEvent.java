package chemical_detector.event;

import chemical_detector.types.Angle;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Loc;
import java.util.List;

/**
 * Events a controller can receive in one control cycle. {@code gas},
 * {@code obstacle} and {@code tick} come from the Vehicle; {@code turn},
 * {@code stop} and {@code resume} are sent by the gas-analysis controller to
 * the movement controller.
 */
public sealed interface InputEvent {

    /** A multi-sensor gas reading. */
    record gas(List<GasSensor> gs) implements InputEvent {
    }

    /** An obstacle detected on side {@code l}. */
    record obstacle(Loc l) implements InputEvent {
    }

    /** Command to head in direction {@code a}. */
    record turn(Angle a) implements InputEvent {
    }

    /** The chemical source has been confirmed. */
    record stop() implements InputEvent {
    }

    /** The last reading showed no target chemical; keep searching. */
    record resume() implements InputEvent {
    }

    /** A control cycle elapsed with no other news from the platform. */
    record tick() implements InputEvent {
    }
}
