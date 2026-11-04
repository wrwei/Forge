package chemical_detector.event;

import chemical_detector.domain.Angle;

/** Events a controller sends: one record per event. */
public sealed interface OutputEvent {

    /**
     * The direction of the strongest signal, sent by the gas-analysis
     * subsystem to the movement subsystem (CD-Evt4).
     *
     * @param direction the direction to face next
     */
    record Turn(Angle direction) implements OutputEvent {}

    /** Sent by the gas-analysis subsystem when the source is confirmed (CD-Evt5). */
    record Stop() implements OutputEvent {}

    /** Sent by the gas-analysis subsystem after a no-gas reading (CD-Evt6). */
    record Resume() implements OutputEvent {}

    /** Sent by the movement subsystem to the Vehicle when the source is found (CD-Evt7). */
    record Flag() implements OutputEvent {}
}
