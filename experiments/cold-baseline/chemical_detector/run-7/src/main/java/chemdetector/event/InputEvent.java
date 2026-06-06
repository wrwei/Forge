package chemdetector.event;

import chemdetector.domain.Angle;
import chemdetector.domain.GasSensor;
import chemdetector.domain.Loc;

import java.util.List;

/**
 * Sealed-interface enumeration of every event the Chemical Detector
 * controllers can receive. Each variant maps to one event in the
 * generated RoboChart model.
 *
 * Boundary inputs (Vehicle -> controllers):
 *   - Gas      (CD-Evt1)
 *   - Obstacle (CD-Evt2)
 *   - Odometer (CD-Evt3)
 *
 * Inter-controller events (gas analysis <-> movement):
 *   - Turn   (CD-Evt4)
 *   - Stop   (CD-Evt5)
 *   - Resume (CD-Evt6)
 */
public sealed interface InputEvent {

    /** CD-Evt1 — gas reading from the sensor array. */
    record Gas(List<GasSensor> gs) implements InputEvent {
    }

    /** CD-Evt2 — obstacle detected on side l. */
    record Obstacle(Loc l) implements InputEvent {
    }

    /** CD-Evt3 — cumulative distance travelled. */
    record Odometer(double distance) implements InputEvent {
    }

    /** CD-Evt4 — direction command from gas analysis. */
    record Turn(Angle a) implements InputEvent {
    }

    /** CD-Evt5 — gas analysis confirms source found. */
    record Stop() implements InputEvent {
    }

    /** CD-Evt6 — gas analysis releases movement back to searching. */
    record Resume() implements InputEvent {
    }
}
