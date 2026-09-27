package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Loc;
import java.util.List;

/**
 * Events received by a controller. {@code Gas} and {@code Obstacle} come from the
 * Vehicle; {@code Turn}, {@code Stop} and {@code Resume} are sent by the gas-analysis
 * subsystem to the movement subsystem.
 */
public sealed interface InputEvent {
    record Gas(List<GasSample> gs) implements InputEvent {}
    record Obstacle(Loc l) implements InputEvent {}
    record Turn(Angle a) implements InputEvent {}
    record Stop() implements InputEvent {}
    record Resume() implements InputEvent {}
}
