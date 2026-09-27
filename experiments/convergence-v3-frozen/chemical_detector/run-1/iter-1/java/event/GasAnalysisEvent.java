package chemical_detector.event;

import chemical_detector.data.GasSensor;
import java.util.List;

/**
 * Events consumed by the gas-analysis subsystem. {@code Gas} carries one
 * multi-sensor reading emitted by the Vehicle (CD-Evt1, CD-DM7).
 */
public sealed interface GasAnalysisEvent {

    record Gas(List<GasSensor> gs) implements GasAnalysisEvent {
    }
}
