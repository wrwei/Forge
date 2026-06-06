package chemdetector.event;

import chemdetector.data.GasSensor;

import java.util.List;

/**
 * Input events consumed by the gas-analysis controller. The only
 * boundary input is the gas reading from the Vehicle (CD-Evt1).
 */
public sealed interface GAInputEvent {

    /** CD-Evt1: gas event with multi-sensor reading payload. */
    record Gas(List<GasSensor> reading) implements GAInputEvent {
    }
}
