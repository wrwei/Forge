package chemdetector.event;

import java.util.List;
import chemdetector.datatype.GasSensor;

/**
 * Input events consumed by the gas-analysis controller.
 *
 * <p>CD-Evt1: {@link Gas} carries a sequence of GasSensor values produced
 * by the Vehicle. No external triggers besides {@code gas} drive this
 * subsystem.
 */
public sealed interface GasAnalysisInputEvent {

    /** CD-Evt1. Gas reading from the Vehicle. */
    record Gas(List<GasSensor> reading) implements GasAnalysisInputEvent {
    }
}
