package chemical_detector.event;

import chemical_detector.domain.Angle;

/** Events emitted by the controllers: one record per event. */
public sealed interface OutputEvent {
    /** turn (CD-Evt4): emitted by gas analysis with the direction of the strongest signal. */
    record Turn(Angle direction) implements OutputEvent {}

    /** stop (CD-Evt5): emitted by gas analysis when the peak intensity reaches thr. */
    record Stop() implements OutputEvent {}

    /** resume (CD-Evt6): emitted by gas analysis after a no-gas reading. */
    record Resume() implements OutputEvent {}

    /** flag (CD-Evt7): emitted by movement to the Vehicle on entering Found. */
    record Flag() implements OutputEvent {}
}
