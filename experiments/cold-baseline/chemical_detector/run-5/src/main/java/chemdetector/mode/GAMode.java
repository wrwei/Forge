package chemdetector.mode;

/**
 * Operating modes of the gas-analysis subsystem.
 * Reading is the initial mode (CD-GA-Beh1). Final is the post-stop terminal mode.
 */
public enum GAMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Final
}
