package chemical_detector.event;

import chemical_detector.domain.GasSensor;

import java.util.List;

/**
 * Inputs of the gas-analysis subsystem.
 */
public sealed interface GasAnalysisEvent {

    /** A multi-sensor gas reading; list position (1-based) identifies the sensing direction. */
    record Gas(List<GasSensor> reading) implements GasAnalysisEvent {
        public Gas {
            reading = List.copyOf(reading);
        }
    }

    /** A control cycle in which no gas reading arrived. */
    record NoReading() implements GasAnalysisEvent {
    }
}
