package chemdetector.controller.mode;

/**
 * Modes of the gas-analysis subsystem (CD-GA-FR1..FR4 plus the final state j1).
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    J1
}
