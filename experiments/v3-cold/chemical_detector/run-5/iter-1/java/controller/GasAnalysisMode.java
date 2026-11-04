package chemical_detector.controller;

/** States of the gas-analysis subsystem; the initial state first. */
public enum GasAnalysisMode {
    READING,
    ANALYSING,
    NO_GAS,
    GAS_DETECTED,
    CONCLUDED
}
