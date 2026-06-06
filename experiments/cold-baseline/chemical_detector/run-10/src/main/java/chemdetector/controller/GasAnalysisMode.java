package chemdetector.controller;

/**
 * Modes of the gas-analysis subsystem (CD-GA-FR1..4).
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Final
}
