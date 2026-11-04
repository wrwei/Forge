package chemical_detector.controller;

/** States of the gas-analysis subsystem; Reading is initial (CD-GA-FR1..4). */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
