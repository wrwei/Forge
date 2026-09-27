package chemical_detector.mode;

/** Operating modes of the gas-analysis subsystem; Reading is initial. */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
