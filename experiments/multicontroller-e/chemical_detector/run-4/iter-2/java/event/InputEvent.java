package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;
import java.util.List;

/**
 * Events a controller receives: from the Vehicle (gas, obstacle) or from the
 * gas-analysis subsystem (turn, stop, resume).
 */
public sealed interface InputEvent {
    record Gas(List<GasSensor> reading) implements InputEvent {}
    record Obstacle(Loc side) implements InputEvent {}
    record Turn(Angle angle) implements InputEvent {}
    record Stop() implements InputEvent {}
    record Resume() implements InputEvent {}
}
