package chemdetector.event;

import java.util.List;

import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Loc;

/**
 * Events consumed by the controllers. Gas and Obstacle arrive from the
 * Vehicle boundary (CD-Evt1, CD-Evt2); Turn, Stop, and Resume are the
 * inter-subsystem events emitted by the gas-analysis subsystem and
 * consumed by the movement subsystem (CD-Evt4..6).
 */
public sealed interface InputEvent {

    record Gas(List<GasSensor> reading) implements InputEvent {
    }

    record Obstacle(Loc side) implements InputEvent {
    }

    record Turn(Angle angle) implements InputEvent {
    }

    record Stop() implements InputEvent {
    }

    record Resume() implements InputEvent {
    }
}
