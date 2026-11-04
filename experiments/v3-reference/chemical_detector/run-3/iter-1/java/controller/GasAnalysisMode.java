package chemical_detector.controller;

/** States of the gas-analysis subsystem; the initial state first. */
public enum GasAnalysisMode {
    /** Awaiting the next gas reading (CD-GA-FR2). */
    READING,
    /** Classifying the most recent reading (CD-GA-FR3). */
    ANALYSING,
    /** The reading did not indicate the target chemical (CD-GA-FR1). */
    NO_GAS,
    /** The reading indicated the target chemical (CD-GA-FR4). */
    GAS_DETECTED,
    /** The source is found; no further readings are processed (CD-GA-Beh6). */
    CONCLUDED
}
