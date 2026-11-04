package chemical_detector.controller;

/** States of the gas-analysis subsystem; the initial state first. */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
