package chemdetector.event;

import chemdetector.domain.Angle;

/** Events emitted by the reasoning subsystems. */
public sealed interface OutputEvent {

    /** Direction of the strongest detected signal (CD-Evt4). */
    record Turn(Angle direction) implements OutputEvent {
    }

    /** Chemical source confirmed; movement subsystem must halt (CD-Evt5). */
    record Stop() implements OutputEvent {
    }

    /** No-gas reading analysed; movement subsystem returns to searching (CD-Evt6). */
    record Resume() implements OutputEvent {
    }

    /** Chemical source located; the platform may halt and report (CD-Evt7). */
    record Flag() implements OutputEvent {
    }
}
