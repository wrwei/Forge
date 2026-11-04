package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Loc;
import java.util.List;

/**
 * Events received by the controllers: gas and obstacle from the Vehicle,
 * turn, stop and resume from the gas-analysis subsystem.
 */
public sealed interface InputEvent {
    record Gas(List<GasSample> reading) implements InputEvent {}
    record Obstacle(Loc side) implements InputEvent {}
    record Turn(Angle direction) implements InputEvent {}
    record Stop() implements InputEvent {}
    record Resume() implements InputEvent {}
}
