package chemdetector.event;

import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Loc;
import java.util.List;

/**
 * Events received by the controllers. Boundary inputs come from the
 * Vehicle (gas, obstacle); turn, stop and resume are emitted by the
 * gas-analysis subsystem and consumed by the movement subsystem.
 */
public sealed interface InputEvent {

    /** One multi-sensor gas reading from the Vehicle. */
    record gas(List<GasSensor> value) implements InputEvent {
    }

    /** Side on which an obstacle has been detected. */
    record obstacle(Loc value) implements InputEvent {
    }

    /** Direction the robot should face next. */
    record turn(Angle value) implements InputEvent {
    }

    /** Chemical source confirmed; halt. */
    record stop() implements InputEvent {
    }

    /** No-gas reading analysed; continue searching. */
    record resume() implements InputEvent {
    }
}
