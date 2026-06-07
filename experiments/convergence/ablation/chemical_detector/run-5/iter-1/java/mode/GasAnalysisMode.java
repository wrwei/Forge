package chemdetector.mode;

/**
 * Operating modes of the gas-analysis subsystem (CD-GA-FR1..4).
 * Stopped is the live terminal mode entered after the source is confirmed;
 * it consumes and discards further gas readings.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Stopped
}
