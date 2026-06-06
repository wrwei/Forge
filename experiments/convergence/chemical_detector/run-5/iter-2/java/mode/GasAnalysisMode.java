package chemdetector.mode;

/**
 * Operating modes of the gas-analysis subsystem (CD-GA-FR1..CD-GA-FR4).
 * Reading is the initial mode.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected
}
