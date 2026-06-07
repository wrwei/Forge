package chemdetector.mode;

/**
 * Operating modes of the gas-analysis subsystem. {@code Concluded} is the
 * live terminal mode entered once the chemical source has been confirmed;
 * further readings are consumed without effect.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
