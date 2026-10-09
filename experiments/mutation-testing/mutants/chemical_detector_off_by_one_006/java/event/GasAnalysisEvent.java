package chemdetector.event;

import chemdetector.data.GasSensor;
import java.util.List;

/**
 * Sealed input alphabet of the gas-analysis subsystem.
 * <p>
 * The gas-analysis subsystem only receives the {@code gas} event from the
 * Vehicle (CD-Evt1). Autonomous transitions (NoGas -> Reading, Analysis ->
 * NoGas/GasDetected, GasDetected -> j1/Reading) are handled at {@code step()}
 * time without an event payload, modelled here by {@link Tick}.
 */
public sealed interface GasAnalysisEvent {

    /** CD-Evt1: Vehicle sends one multi-sensor gas reading. */
    record Gas(List<GasSensor> payload) implements GasAnalysisEvent {
    }

    /** Cycle tick that lets autonomous transitions fire. */
    record Tick() implements GasAnalysisEvent {
    }
}
