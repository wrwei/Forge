package chemical_detector.mode;

/**
 * Modes of the gas-analysis controller; Reading is the initial mode.
 * Concluded is the terminal mode reached once the source is confirmed.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
