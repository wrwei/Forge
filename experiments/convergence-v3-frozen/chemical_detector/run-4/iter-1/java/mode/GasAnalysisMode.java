package chemical_detector.mode;

/** Operating modes of the gas-analysis subsystem; READING is initial. */
public enum GasAnalysisMode {
    READING,
    ANALYSIS,
    NO_GAS,
    GAS_DETECTED,
    CONCLUDED
}
