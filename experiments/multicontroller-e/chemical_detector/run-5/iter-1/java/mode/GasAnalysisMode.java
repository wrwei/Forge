package chemical_detector.mode;

/** States of the gas-analysis subsystem (CD-GA-FR1..4); Reading is initial. */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
