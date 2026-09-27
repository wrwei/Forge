package chemical_detector.mode;

/** States of the gas-analysis subsystem; {@code Reading} is initial (CD-GA-FR1..4). */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Concluded
}
