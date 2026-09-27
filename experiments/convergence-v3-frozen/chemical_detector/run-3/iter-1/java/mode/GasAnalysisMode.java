package chemical_detector.mode;

/**
 * States of the gas-analysis subsystem; READING is initial.
 */
public enum GasAnalysisMode {
    READING,
    ANALYSIS,
    NO_GAS,
    GAS_DETECTED,
    SOURCE_FOUND,
    CONCLUDED
}
