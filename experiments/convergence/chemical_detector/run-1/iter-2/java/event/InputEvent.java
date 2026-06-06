package chemdetector.event;

import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Loc;
import java.util.List;

/**
 * Events consumed by the controllers. Gas and Obstacle are emitted by the
 * Vehicle (CD-Evt1, CD-Evt2); Turn, Stop, and Resume are emitted by the
 * gas-analysis subsystem and consumed by the movement subsystem
 * (CD-Evt4..6).
 */
public sealed interface InputEvent {

    /** One multi-sensor gas reading (CD-Evt1). */
    record Gas(List<GasSensor> reading) implements InputEvent {
    }

    /** Obstacle detected at the given side (CD-Evt2). */
    record Obstacle(Loc payload) implements InputEvent {
    }

    /** Direction the robot should face next (CD-Evt4). */
    record Turn(Angle payload) implements InputEvent {
    }

    /** Chemical source confirmed; halt (CD-Evt5). */
    record Stop() implements InputEvent {
    }

    /** No-gas reading analysed; continue searching (CD-Evt6). */
    record Resume() implements InputEvent {
    }
}
