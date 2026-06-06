package chemdetector.mode;

/**
 * Modes for the gas-analysis state machine (CD-GA-FR1..4 + Final).
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Final
}
