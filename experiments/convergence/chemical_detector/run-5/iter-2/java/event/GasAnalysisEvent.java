package chemdetector.event;

import java.util.List;

import chemdetector.datamodel.GasSensor;

/**
 * Input events consumed by the gas-analysis subsystem.
 */
public sealed interface GasAnalysisEvent {

    /** One multi-sensor gas reading (CD-Evt1, CD-DM7). */
    record Gas(List<GasSensor> value) implements GasAnalysisEvent {
    }
}
