package chemical_detector.event;

import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;
import java.util.List;

/** Events received by the controllers. */
public sealed interface InputEvent {
    /** Multi-sensor gas reading from the Vehicle, received by gas analysis (CD-Evt1). */
    record Gas(List<GasSensor> reading) implements InputEvent {}

    /** Side of a detected obstacle from the Vehicle, received by movement (CD-Evt2). */
    record Obstacle(Loc side) implements InputEvent {}

    /** Direction to face next, sent by gas analysis, received by movement (CD-Evt4). */
    record Turn(Angle heading) implements InputEvent {}

    /** Chemical source confirmed, sent by gas analysis, received by movement (CD-Evt5). */
    record Stop() implements InputEvent {}

    /** No gas in the last reading, sent by gas analysis, received by movement (CD-Evt6). */
    record Resume() implements InputEvent {}
}
