package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Loc;
import java.util.List;

/**
 * Events a controller receives. {@code gas} and {@code obstacle} come from the Vehicle
 * (CD-Evt1, CD-Evt2); {@code turn}, {@code stop} and {@code resume} are sent by the
 * gas-analysis subsystem to the movement subsystem (CD-Evt4..6).
 */
public sealed interface InputEvent {
    record gas(List<GasSample> reading) implements InputEvent {}
    record obstacle(Loc side) implements InputEvent {}
    record turn(Angle direction) implements InputEvent {}
    record stop() implements InputEvent {}
    record resume() implements InputEvent {}
}
