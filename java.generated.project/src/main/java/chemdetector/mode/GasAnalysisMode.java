package chemdetector.mode;

/**
 * Operating modes of the gas-analysis subsystem state machine
 * (CD-GA-FR1..4). {@code Final} is the terminal (final) state j1.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Final
}
