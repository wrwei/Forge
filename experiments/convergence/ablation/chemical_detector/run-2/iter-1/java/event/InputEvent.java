package chemdetector.event;

import chemdetector.data.Angle;
import chemdetector.data.GasSensor;
import chemdetector.data.Loc;
import java.util.List;

/**
 * Events received by the controllers. {@code Gas} and {@code Obstacle}
 * arrive from the Vehicle (CD-Evt1, CD-Evt2); {@code Turn}, {@code Stop}
 * and {@code Resume} are emitted by the gas-analysis subsystem and
 * consumed by the movement subsystem (CD-Evt4, CD-Evt5, CD-Evt6).
 */
public sealed interface InputEvent {
    record Gas(List<GasSensor> reading) implements InputEvent {}
    record Obstacle(Loc side) implements InputEvent {}
    record Turn(Angle direction) implements InputEvent {}
    record Stop() implements InputEvent {}
    record Resume() implements InputEvent {}
}
