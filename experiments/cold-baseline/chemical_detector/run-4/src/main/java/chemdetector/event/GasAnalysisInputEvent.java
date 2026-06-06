package chemdetector.event;

import chemdetector.datatype.GasSensor;
import java.util.List;

/**
 * Input events consumed by the gas-analysis state machine.
 * Only the gas event drives the gas-analysis subsystem (CD-Evt1).
 */
public sealed interface GasAnalysisInputEvent {
    record Gas(List<GasSensor> reading) implements GasAnalysisInputEvent {}
}
