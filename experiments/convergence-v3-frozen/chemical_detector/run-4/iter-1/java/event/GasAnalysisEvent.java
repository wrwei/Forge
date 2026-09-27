package chemical_detector.event;

import chemical_detector.data.GasSensor;
import java.util.List;

/** Input events consumed by the gas-analysis subsystem. */
public sealed interface GasAnalysisEvent {

    /** A multi-sensor gas reading: position in the list encodes the sensing direction. */
    record Gas(List<GasSensor> reading) implements GasAnalysisEvent {
    }
}
