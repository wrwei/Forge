package chemical_detector.controller;

/**
 * The states of the gas-analysis subsystem; Reading is the initial state.
 */
public enum GasAnalysisMode {
    /** CD-GA-FR2: waiting for the next gas reading. */
    Reading,
    /** CD-GA-FR3: classifying the most recent reading. */
    Analysis,
    /** CD-GA-FR1: the reading did not indicate the target chemical. */
    NoGas,
    /** CD-GA-FR4: the reading indicated the target chemical; its peak intensity is evaluated. */
    GasDetected,
    /** CD-GA-Beh6: the source has been found; no further readings are processed. */
    Concluded
}
