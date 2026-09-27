package chemical_detector.mode;

/**
 * Modes of the gas-analysis subsystem (CD-GA-FR1..4). {@code Reading} is initial.
 * {@code Concluded} is entered once the source is confirmed; no further reading is
 * analysed there.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
