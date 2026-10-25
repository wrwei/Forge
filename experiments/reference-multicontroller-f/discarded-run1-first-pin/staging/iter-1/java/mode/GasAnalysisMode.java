package chemical_detector.mode;

/**
 * Modes of the gas-analysis subsystem; READING is the initial mode.
 * CONCLUDED is the terminal mode entered once the chemical source is confirmed.
 */
public enum GasAnalysisMode {
    READING,
    ANALYSIS,
    NO_GAS,
    GAS_DETECTED,
    CONCLUDED
}
