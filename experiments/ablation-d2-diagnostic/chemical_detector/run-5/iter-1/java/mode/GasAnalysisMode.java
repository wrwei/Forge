package chemical_detector.mode;

/** States of the gas-analysis subsystem; {@code Reading} is initial. */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
