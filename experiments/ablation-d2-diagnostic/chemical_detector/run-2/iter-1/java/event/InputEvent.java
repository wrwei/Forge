package chemical_detector.event;

import chemical_detector.data.Angle;
import chemical_detector.data.GasSample;
import chemical_detector.data.Loc;
import java.util.List;

/**
 * Events a controller receives. {@code gas} and {@code obstacle} come from the
 * Vehicle; {@code turn}, {@code stop} and {@code resume} are sent by the
 * gas-analysis subsystem to the movement subsystem.
 */
public sealed interface InputEvent {
    record gas(List<GasSample> reading) implements InputEvent {}
    record obstacle(Loc side) implements InputEvent {}
    record turn(Angle direction) implements InputEvent {}
    record stop() implements InputEvent {}
    record resume() implements InputEvent {}
}
