package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;

import java.util.List;

/**
 * Events a controller receives. {@code Gas} and {@code Obstacle} come from the
 * Vehicle; {@code Turn}, {@code Stop} and {@code Resume} come from the
 * gas-analysis subsystem and are received by the movement subsystem.
 */
public sealed interface InputEvent {
    /** One multi-sensor gas reading, ordered by sensing direction (CD-Evt1, CD-DM7). */
    record Gas(List<GasSensor> sample) implements InputEvent {}

    /** Side on which an obstacle was detected (CD-Evt2). */
    record Obstacle(Loc side) implements InputEvent {}

    /** Direction the robot should face next (CD-Evt4). */
    record Turn(Angle direction) implements InputEvent {}

    /** The chemical source has been confirmed (CD-Evt5). */
    record Stop() implements InputEvent {}

    /** The last reading had no target chemical; keep searching (CD-Evt6). */
    record Resume() implements InputEvent {}
}
