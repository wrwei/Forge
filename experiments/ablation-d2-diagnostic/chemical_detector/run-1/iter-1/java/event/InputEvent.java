package chemical_detector.event;

import chemical_detector.types.Angle;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Loc;
import java.util.List;

/** Events received by the controllers. */
public sealed interface InputEvent {
    /** Multi-sensor gas reading emitted by the Vehicle. */
    record gas(List<GasSensor> readings) implements InputEvent {
    }

    /** Obstacle detected by the Vehicle on the given side. */
    record obstacle(Loc l) implements InputEvent {
    }

    /** Direction command from gas analysis to movement. */
    record turn(Angle a) implements InputEvent {
    }

    /** Chemical source confirmed; sent from gas analysis to movement. */
    record stop() implements InputEvent {
    }

    /** No gas in the last reading; sent from gas analysis to movement. */
    record resume() implements InputEvent {
    }
}
