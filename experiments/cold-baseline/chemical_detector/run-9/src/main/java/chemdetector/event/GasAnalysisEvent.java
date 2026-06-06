package chemdetector.event;

import chemdetector.datatype.GasSensor;
import java.util.List;

/**
 * Input events consumed by the gas-analysis subsystem.
 * <ul>
 *   <li>CD-Evt1 gas: multi-sensor reading payload.</li>
 * </ul>
 */
public sealed interface GasAnalysisEvent {
    record Gas(List<GasSensor> reading) implements GasAnalysisEvent {}
}
