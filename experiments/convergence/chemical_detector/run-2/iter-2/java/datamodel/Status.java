package chemdetector.datamodel;

/**
 * Outcome of a single gas-analysis cycle (CD-DM1): noGas when the current
 * reading does not indicate the target chemical, gasD when it does.
 */
public enum Status {
    noGas,
    gasD
}
