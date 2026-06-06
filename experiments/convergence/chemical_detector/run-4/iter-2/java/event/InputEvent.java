package chemdetector.event;

import java.util.List;

import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Loc;

/**
 * Events consumed by the controllers. Gas, Obstacle are boundary inputs
 * from the Vehicle; Turn, Stop, Resume are emitted by the gas-analysis
 * subsystem and consumed by the movement subsystem.
 */
public sealed interface InputEvent {

    /** One multi-sensor gas reading. */
    record Gas(List<GasSensor> payload) implements InputEvent {
    }

    /** Side on which an obstacle has been encountered. */
    record Obstacle(Loc payload) implements InputEvent {
    }

    /** Direction the robot should face next. */
    record Turn(Angle payload) implements InputEvent {
    }

    /** Chemical source confirmed: halt. */
    record Stop() implements InputEvent {
    }

    /** No-gas reading analysed: continue searching. */
    record Resume() implements InputEvent {
    }
}
