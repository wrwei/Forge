package chemical_detector.mode;

/**
 * Operating modes of the gas-analysis subsystem. {@code Reading} is initial;
 * {@code Concluded} is entered once the chemical source has been confirmed.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
