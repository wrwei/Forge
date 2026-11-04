package chemical_detector.mode;

/**
 * States of the gas-analysis subsystem; Reading is initial.
 * Concluded is the final state reached once the source is found.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
