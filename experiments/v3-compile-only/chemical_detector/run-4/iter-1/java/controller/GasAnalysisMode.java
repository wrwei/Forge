package chemical_detector.controller;

/**
 * States of the gas-analysis subsystem; the initial state first.
 */
public enum GasAnalysisMode {
    /** Awaiting the next gas reading (CD-GA-FR2). */
    READING,
    /** Classifying the most recent reading (CD-GA-FR3). */
    ANALYSIS,
    /** The reading showed no target chemical (CD-GA-FR1). */
    NO_GAS,
    /** The reading showed the target chemical (CD-GA-FR4). */
    GAS_DETECTED,
    /** Final: the source is found; no further readings (CD-GA-Beh6). */
    CONCLUDED
}
