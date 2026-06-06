package chemdetector.event;

import chemdetector.data.GasSensor;
import java.util.List;

/**
 * Inputs received by the gas-analysis controller's step() method.
 * The only environmental input is the multi-sensor gas reading from the Vehicle (CD-Evt1).
 */
public sealed interface GAInputEvent {

    /** CD-Evt1 — gas event carrying a Seq(GasSensor). */
    record Gas(List<GasSensor> gs) implements GAInputEvent {
    }
}
