package chemdetector.event;

import chemdetector.domain.GasSensor;
import java.util.List;

/**
 * Inputs received by the gas-analysis controller. The {@code Gas} event
 * carries the latest multi-sensor reading (CD-Evt1).
 */
public sealed interface GasAnalysisInputEvent {

    /** CD-Evt1 — multi-sensor gas reading published by the Vehicle. */
    record Gas(List<GasSensor> gs) implements GasAnalysisInputEvent {}
}
