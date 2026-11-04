package chemical_detector.controller;

/** Modes of the gas-analysis subsystem; the initial mode first. */
public enum GasAnalysisMode {
    READING,
    ANALYSING,
    NO_GAS,
    GAS_DETECTED,
    CONCLUDED
}
