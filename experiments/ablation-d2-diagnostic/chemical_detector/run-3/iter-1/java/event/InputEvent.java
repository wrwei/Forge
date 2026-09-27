package chemical_detector.event;

import chemical_detector.data.Angle;
import chemical_detector.data.GasSample;
import chemical_detector.data.Loc;
import java.util.List;

/** Events delivered to a controller's {@code step} method, one per control cycle. */
public sealed interface InputEvent {

    /** A multi-sensor gas reading; position in the list encodes the sensing direction. */
    record Gas(List<GasSample> gs) implements InputEvent {
        public Gas {
            gs = List.copyOf(gs);
        }
    }

    /** An obstacle detected on side {@code l}. */
    record Obstacle(Loc l) implements InputEvent {}

    /** Command from gas analysis to face direction {@code a}. */
    record Turn(Angle a) implements InputEvent {}

    /** Gas analysis has confirmed the chemical source. */
    record Stop() implements InputEvent {}

    /** Gas analysis found no target chemical; resume searching. */
    record Resume() implements InputEvent {}

    /** No event arrived during this control cycle. */
    record NoEvent() implements InputEvent {}
}
