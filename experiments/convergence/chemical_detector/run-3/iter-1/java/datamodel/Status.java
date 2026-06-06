package chemdetector.datamodel;

/**
 * Outcome of a single gas-analysis cycle: noGas when the current reading
 * does not indicate the target chemical, gasD when it does.
 */
public enum Status {
    noGas,
    gasD
}
