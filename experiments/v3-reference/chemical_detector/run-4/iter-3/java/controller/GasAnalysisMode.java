package chemical_detector.controller;

/**
 * States of the gas-analysis subsystem (CD-GA-FR1..4); Reading is initial and
 * Concluded is the final state reached once the source is found.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
