package chemdetector.mode;

/**
 * Operating modes of the gas-analysis subsystem (CD-GA-FR1..4). Done is the
 * terminal live mode entered once the chemical source has been confirmed
 * (replaces the specification's final state j1 so the extracted state
 * machine has no Final state).
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Done
}
