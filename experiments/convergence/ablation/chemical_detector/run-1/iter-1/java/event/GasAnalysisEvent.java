package chemdetector.event;

import java.util.List;

import chemdetector.datamodel.GasSensor;

/** Events consumed by the gas-analysis subsystem. */
public sealed interface GasAnalysisEvent {

    /** One multi-sensor gas reading emitted by the Vehicle. */
    record Gas(List<GasSensor> reading) implements GasAnalysisEvent {
    }
}
