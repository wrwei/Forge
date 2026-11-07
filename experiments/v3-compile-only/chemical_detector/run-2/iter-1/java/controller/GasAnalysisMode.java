package chemical_detector.controller;

/** States of the gas-analysis subsystem; Reading is the initial state. */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
