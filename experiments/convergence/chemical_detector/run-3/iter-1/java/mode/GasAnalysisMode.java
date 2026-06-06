package chemdetector.mode;

/**
 * Operating modes of the gas-analysis subsystem. Located is the live
 * terminal mode entered when the chemical source has been confirmed
 * (the specification's final state, kept live for model extraction).
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Located
}
