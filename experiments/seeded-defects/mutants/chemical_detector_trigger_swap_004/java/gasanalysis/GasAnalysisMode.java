package chemdetector.gasanalysis;

/**
 * Modes of the gas-analysis state machine
 * (CD-GA-FR1..4, plus the {@link #Final} sink that absorbs the {@code j1}
 * final state).
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Final
}
