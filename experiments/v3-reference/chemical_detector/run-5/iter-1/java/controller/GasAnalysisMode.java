package chemical_detector.controller;

/**
 * Modes of the gas-analysis subsystem; the initial mode first.
 */
public enum GasAnalysisMode {
    /** Awaiting the next gas reading (initial). */
    Reading,
    /** Classifying the most recent reading. */
    Analysis,
    /** The reading showed no target chemical. */
    NoGas,
    /** The reading showed the target chemical; its peak intensity is evaluated. */
    GasDetected,
    /** The source has been found; no further readings are processed (terminal). */
    Concluded
}
