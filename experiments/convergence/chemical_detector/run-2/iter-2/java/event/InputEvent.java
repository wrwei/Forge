package chemdetector.event;

import java.util.List;

import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Loc;

/**
 * Events received by the Chemical Detector controllers. Gas, Obstacle come
 * from the Vehicle's sensors (CD-Evt1, CD-Evt2); Turn, Stop, Resume are the
 * inter-subsystem events emitted by the gas-analysis subsystem and consumed
 * by the movement subsystem (CD-Evt4..6).
 */
public sealed interface InputEvent {

    /** One multi-sensor gas reading (CD-Evt1). */
    record Gas(List<GasSensor> value) implements InputEvent {
    }

    /** Obstacle detected on the given side (CD-Evt2). */
    record Obstacle(Loc value) implements InputEvent {
    }

    /** Direction the robot should face next (CD-Evt4). */
    record Turn(Angle value) implements InputEvent {
    }

    /** Chemical source confirmed; halt (CD-Evt5). */
    record Stop() implements InputEvent {
    }

    /** No-gas reading analysed; continue searching (CD-Evt6). */
    record Resume() implements InputEvent {
    }
}
