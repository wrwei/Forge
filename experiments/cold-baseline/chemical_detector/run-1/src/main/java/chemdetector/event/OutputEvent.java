package chemdetector.event;

import chemdetector.datatype.Angle;

/**
 * Sealed output-event hierarchy.
 *
 * Inter-controller signals emitted by the gas-analysis subsystem (shared with movement):
 *   - Turn   (CD-Evt4)
 *   - Stop   (CD-Evt5)
 *   - Resume (CD-Evt6)
 *
 * Boundary output emitted by the movement subsystem to the Vehicle:
 *   - Flag   (CD-Evt7)
 */
public sealed interface OutputEvent {

    /** CD-Evt4: gas-analysis tells movement which angle to face. */
    record Turn(Angle payload) implements OutputEvent {
    }

    /** CD-Evt5: gas-analysis tells movement to halt. */
    record Stop() implements OutputEvent {
    }

    /** CD-Evt6: gas-analysis tells movement to keep searching. */
    record Resume() implements OutputEvent {
    }

    /** CD-Evt7: movement tells the Vehicle the chemical source has been located. */
    record Flag() implements OutputEvent {
    }
}
