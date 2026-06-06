package chemdetector.event;

import chemdetector.datatype.Angle;
import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Loc;
import java.util.List;

/**
 * Sealed input-event hierarchy.
 *
 * Boundary inputs (from the Vehicle, type "boundary"):
 *   - Gas      (CD-Evt1) -> gas-analysis subsystem
 *   - Obstacle (CD-Evt2) -> movement subsystem
 *   - Odometer (CD-Evt3) -> movement subsystem
 *
 * Inter-controller events (gas-analysis -> movement, type "shared"):
 *   - Turn     (CD-Evt4)
 *   - Stop     (CD-Evt5)
 *   - Resume   (CD-Evt6)
 */
public sealed interface InputEvent {

    /** CD-Evt1: gas event carrying a multi-sensor reading. */
    record Gas(List<GasSensor> payload) implements InputEvent {
    }

    /** CD-Evt2: obstacle event carrying the side where the obstacle is. */
    record Obstacle(Loc payload) implements InputEvent {
    }

    /** CD-Evt3: odometer event carrying the cumulative distance travelled. */
    record Odometer(double payload) implements InputEvent {
    }

    /** CD-Evt4: turn event carrying the angle to face next. */
    record Turn(Angle payload) implements InputEvent {
    }

    /** CD-Evt5: stop signal — chemical source confirmed. */
    record Stop() implements InputEvent {
    }

    /** CD-Evt6: resume signal — keep searching. */
    record Resume() implements InputEvent {
    }
}
