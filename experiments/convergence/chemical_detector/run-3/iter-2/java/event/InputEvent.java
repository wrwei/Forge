package chemdetector.event;

import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Loc;
import java.util.List;

/**
 * Events consumed by the controllers. Gas and Obstacle come from the
 * Vehicle's sensors; Turn, Stop and Resume are emitted by the
 * gas-analysis subsystem and consumed by the movement subsystem.
 */
public sealed interface InputEvent {

    /** One multi-sensor gas reading (sequence of GasSensor values). */
    record Gas(List<GasSensor> reading) implements InputEvent {
    }

    /** Obstacle encountered on the given side of the robot. */
    record Obstacle(Loc side) implements InputEvent {
    }

    /** Direction the robot should face next. */
    record Turn(Angle direction) implements InputEvent {
    }

    /** Chemical source confirmed — halt. */
    record Stop() implements InputEvent {
    }

    /** No gas in the last reading — continue searching. */
    record Resume() implements InputEvent {
    }
}
