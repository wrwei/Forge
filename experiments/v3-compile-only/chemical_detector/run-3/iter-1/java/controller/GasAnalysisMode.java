package chemical_detector.controller;

/** States of the gas-analysis subsystem; Reading is initial. */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Final
}
