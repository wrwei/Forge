package chemical_detector.event;

import chemical_detector.domain.Angle;

/** Events emitted by the controllers. */
public sealed interface OutputEvent {
    /** Direction of the strongest signal, emitted by gas analysis (CD-Evt4). */
    record Turn(Angle heading) implements OutputEvent {}

    /** Chemical source confirmed, emitted by gas analysis (CD-Evt5). */
    record Stop() implements OutputEvent {}

    /** Reading classified as no gas, emitted by gas analysis (CD-Evt6). */
    record Resume() implements OutputEvent {}

    /** Chemical source located, emitted by movement to the Vehicle (CD-Evt7). */
    record Flag() implements OutputEvent {}
}
