package chemdetector.mode;

/**
 * Operating modes of the gas-analysis subsystem.
 *
 * <p>Done replaces the specification's final state j1: the chemical
 * source has been confirmed and the stop signal sent; the machine stays
 * live, absorbing further gas readings without acting on them.
 */
public enum GasAnalysisMode {
    Reading,
    Analysis,
    NoGas,
    GasDetected,
    Done
}
