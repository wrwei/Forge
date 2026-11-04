package chemical_detector.mode;

/**
 * Modes of the gas-analysis subsystem; the initial mode first.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
