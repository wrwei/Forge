package chemdetector.event;

import chemdetector.domain.Angle;
import chemdetector.domain.GasSensor;
import chemdetector.domain.Loc;
import java.util.List;

/** Events received by the reasoning subsystems. */
public sealed interface InputEvent {

    /** One multi-sensor gas reading from the Vehicle (CD-Evt1). */
    record Gas(List<GasSensor> reading) implements InputEvent {
    }

    /** Side on which the Vehicle has detected an obstacle (CD-Evt2). */
    record Obstacle(Loc side) implements InputEvent {
    }

    /** Direction the robot should face next (CD-Evt4). */
    record Turn(Angle direction) implements InputEvent {
    }

    /** Chemical source confirmed; halt (CD-Evt5). */
    record Stop() implements InputEvent {
    }

    /** No-gas reading analysed; continue searching (CD-Evt6). */
    record Resume() implements InputEvent {
    }
}
