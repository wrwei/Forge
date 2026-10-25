package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;
import java.util.List;

/**
 * Events a controller consumes in one control cycle.
 * Gas and Obstacle come from the Vehicle; Turn, Stop and Resume come from the
 * gas-analysis subsystem; Tick is a control cycle that carries no other event.
 */
public sealed interface InputEvent {
    record Gas(List<GasSensor> reading) implements InputEvent {}
    record Obstacle(Loc loc) implements InputEvent {}
    record Turn(Angle angle) implements InputEvent {}
    record Stop() implements InputEvent {}
    record Resume() implements InputEvent {}
    record Tick() implements InputEvent {}
}
