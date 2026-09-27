package chemical_detector.event;

import chemical_detector.data.Angle;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Loc;
import java.util.List;

/** Events a controller can receive. */
public sealed interface InputEvent {

    /** gas: one multi-sensor reading from the Vehicle (CD-Evt1). */
    record Gas(List<GasSensor> readings) implements InputEvent {
    }

    /** obstacle: side on which the Vehicle detected an obstacle (CD-Evt2). */
    record Obstacle(Loc loc) implements InputEvent {
    }

    /** turn: direction the robot should face next, sent by gas analysis (CD-Evt4). */
    record Turn(Angle angle) implements InputEvent {
    }

    /** stop: the chemical source has been confirmed (CD-Evt5). */
    record Stop() implements InputEvent {
    }

    /** resume: the last reading showed no gas, keep searching (CD-Evt6). */
    record Resume() implements InputEvent {
    }
}
