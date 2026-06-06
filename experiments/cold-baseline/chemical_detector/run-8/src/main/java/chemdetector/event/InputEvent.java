package chemdetector.event;

import java.util.List;
import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Loc;
import chemdetector.annotation.RoboChartType;

/**
 * Sealed interface of all sensor-originated events incoming to the
 * controllers. Each controller's step() consumes the InputEvent supertype;
 * the controller filters by record subtype with traditional instanceof.
 */
public sealed interface InputEvent {
    record Gas(List<GasSensor> payload) implements InputEvent {}
    record Obstacle(Loc payload) implements InputEvent {}
    record Odometer(@RoboChartType("real") double payload) implements InputEvent {}
    record Turn(chemdetector.datatype.Angle payload) implements InputEvent {}
    record Stop() implements InputEvent {}
    record Resume() implements InputEvent {}
    record Tick() implements InputEvent {}
}
