package chemical_detector.mode;

/** Modes of the gas-analysis subsystem. */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    /** The source has been confirmed; readings are no longer analysed. */
    Concluded
}
