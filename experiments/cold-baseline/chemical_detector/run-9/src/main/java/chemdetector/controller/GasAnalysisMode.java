package chemdetector.controller;

/**
 * Modes of the gas-analysis subsystem (CD-GA-FR1..4 + final).
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Final
}
