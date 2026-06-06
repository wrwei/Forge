package chemdetector.controller;

/**
 * Modes of the gas-analysis controller. Maps to RoboChart states
 * Reading, Analysis, NoGas, GasDetected, plus the final state Final.
 */
public enum GAMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Final
}
