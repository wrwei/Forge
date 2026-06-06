package chemdetector.mode;

/**
 * Operating modes of the gas-analysis state machine.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Final
}
