package chemdetector.controller;

/** Operating modes of the gas-analysis subsystem (CD-GA-FR1..CD-GA-FR4). */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected
}
