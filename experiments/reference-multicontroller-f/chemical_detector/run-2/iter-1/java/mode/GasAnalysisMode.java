package chemical_detector.mode;

/**
 * Modes of the gas-analysis subsystem; Reading is initial. Concluded is the terminal mode
 * entered once the chemical source has been confirmed.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
