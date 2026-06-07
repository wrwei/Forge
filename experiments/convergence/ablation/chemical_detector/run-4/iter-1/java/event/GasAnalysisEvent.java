package chemdetector.event;

import chemdetector.data.GasSensor;
import java.util.List;

/**
 * Events consumed by the gas-analysis subsystem.
 */
public sealed interface GasAnalysisEvent {

    /**
     * One multi-sensor gas reading: an ordered list of {@link GasSensor}
     * values captured at one point in time. The position in the list
     * encodes which sensor produced each value.
     */
    record Gas(List<GasSensor> reading) implements GasAnalysisEvent {
    }
}
