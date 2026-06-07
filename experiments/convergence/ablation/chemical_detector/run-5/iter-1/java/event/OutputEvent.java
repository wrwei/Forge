package chemdetector.event;

import chemdetector.datamodel.Angle;

/**
 * Events emitted by the controllers. Turn, Stop, and Resume are emitted by
 * the gas-analysis subsystem toward the movement subsystem (CD-Evt4..6);
 * Flag is emitted by the movement subsystem to the Vehicle when the source
 * has been located (CD-Evt7).
 */
public sealed interface OutputEvent {

    record Turn(Angle angle) implements OutputEvent {
    }

    record Stop() implements OutputEvent {
    }

    record Resume() implements OutputEvent {
    }

    record Flag() implements OutputEvent {
    }
}
