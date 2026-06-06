package chemdetector.event;

import chemdetector.datamodel.Angle;

/**
 * Events emitted by the Chemical Detector controllers. Turn, Stop, Resume
 * are emitted by the gas-analysis subsystem towards the movement subsystem
 * (CD-Evt4..6); Flag is emitted by the movement subsystem to the Vehicle
 * when the chemical source has been located (CD-Evt7).
 */
public sealed interface OutputEvent {

    /** Steer towards the strongest detected signal (CD-Evt4). */
    record Turn(Angle value) implements OutputEvent {
    }

    /** Chemical source confirmed; movement subsystem must halt (CD-Evt5). */
    record Stop() implements OutputEvent {
    }

    /** Reading contained no target chemical; keep searching (CD-Evt6). */
    record Resume() implements OutputEvent {
    }

    /** Chemical source located; platform may halt and report (CD-Evt7). */
    record Flag() implements OutputEvent {
    }
}
