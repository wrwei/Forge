package chemdetector.event;

import chemdetector.datatype.Angle;

/**
 * Output events emitted by the gas-analysis controller. All three are
 * routed to the movement controller (Shared interface in RoboChart terms).
 */
public sealed interface GasAnalysisOutputEvent {

    /** CD-Evt4. Turn ! anl — direction towards strongest signal. */
    record Turn(Angle value) implements GasAnalysisOutputEvent {
    }

    /** CD-Evt5. Stop — chemical source confirmed. */
    record Stop() implements GasAnalysisOutputEvent {
    }

    /** CD-Evt6. Resume — no-gas reading; continue searching. */
    record Resume() implements GasAnalysisOutputEvent {
    }
}
