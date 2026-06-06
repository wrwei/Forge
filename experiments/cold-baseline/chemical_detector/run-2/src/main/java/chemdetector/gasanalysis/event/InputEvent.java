package chemdetector.gasanalysis.event;

import chemdetector.data.GasSensor;
import java.util.List;

/**
 * Boundary input events the gas-analysis subsystem receives from the
 * Vehicle. CD-Evt1.
 */
public sealed interface InputEvent {

    /** CD-Evt1: gas reading from the Vehicle's gas-sensor array. */
    record Gas(List<GasSensor> gs) implements InputEvent {}
}
