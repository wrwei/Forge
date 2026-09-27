package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;
import java.util.List;

/**
 * Events a controller consumes. {@code gas} and {@code obstacle} come from the Vehicle;
 * {@code turn}, {@code stop} and {@code resume} come from the gas-analysis subsystem.
 */
public sealed interface InputEvent {

    /** One multi-sensor gas reading (CD-Evt1, CD-DM7). */
    record gas(List<GasSensor> readings) implements InputEvent {
    }

    /** Side at which an obstacle was encountered (CD-Evt2). */
    record obstacle(Loc side) implements InputEvent {
    }

    /** Direction the robot should face next (CD-Evt4). */
    record turn(Angle angle) implements InputEvent {
    }

    /** Chemical source confirmed (CD-Evt5). */
    record stop() implements InputEvent {
    }

    /** No-gas reading analysed; keep searching (CD-Evt6). */
    record resume() implements InputEvent {
    }
}
