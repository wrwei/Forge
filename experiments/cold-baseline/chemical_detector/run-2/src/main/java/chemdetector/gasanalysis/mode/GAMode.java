package chemdetector.gasanalysis.mode;

/**
 * States of the gas-analysis subsystem. CD-GA-FR1..4 plus the final
 * sink state j1 (used by CD-GA-Beh6).
 */
public enum GAMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Final
}
