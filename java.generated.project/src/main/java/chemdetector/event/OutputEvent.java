package chemdetector.event;

import chemdetector.datamodel.Angle;

/**
 * Events emitted (as transition actions) by the two subsystem state
 * machines.
 *
 * <ul>
 *   <li>{@code Turn}/{@code Stop}/{@code Resume} — emitted by gas-analysis
 *       towards the movement subsystem (CD-Evt4/CD-Evt5/CD-Evt6).</li>
 *   <li>{@code Flag} — emitted by the movement subsystem to the Vehicle on
 *       entering Found (CD-Evt7).</li>
 * </ul>
 */
public sealed interface OutputEvent {

    /** turn ! anl — steer towards the strongest signal (CD-Evt4). */
    record Turn(Angle angle) implements OutputEvent {
    }

    /** stop — confirm the chemical source (CD-Evt5). */
    record Stop() implements OutputEvent {
    }

    /** resume — return to searching (CD-Evt6). */
    record Resume() implements OutputEvent {
    }

    /** flag — chemical source located (CD-Evt7). */
    record Flag() implements OutputEvent {
    }
}
