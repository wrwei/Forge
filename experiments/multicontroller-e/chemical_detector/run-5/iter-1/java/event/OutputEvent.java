package chemical_detector.event;

import chemical_detector.domain.Angle;

/**
 * Events a controller emits. The gas-analysis subsystem emits {@code turn}, {@code stop}
 * and {@code resume} to the movement subsystem; the movement subsystem emits {@code flag}
 * to the Vehicle.
 */
public sealed interface OutputEvent {

    /** Direction of the strongest signal (CD-Evt4). */
    record turn(Angle angle) implements OutputEvent {
    }

    /** Chemical source confirmed (CD-Evt5). */
    record stop() implements OutputEvent {
    }

    /** No-gas reading analysed; keep searching (CD-Evt6). */
    record resume() implements OutputEvent {
    }

    /** Chemical source located; the platform may halt and report (CD-Evt7). */
    record flag() implements OutputEvent {
    }
}
