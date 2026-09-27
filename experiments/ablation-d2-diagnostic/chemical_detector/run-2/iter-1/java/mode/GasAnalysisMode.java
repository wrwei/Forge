package chemical_detector.mode;

/**
 * States of the gas-analysis subsystem. {@code Concluded} is the state reached
 * once the chemical source has been confirmed.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
