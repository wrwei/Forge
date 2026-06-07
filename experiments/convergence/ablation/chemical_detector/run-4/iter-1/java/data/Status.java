package chemdetector.data;

/**
 * Outcome of a single gas-analysis cycle: {@code noGas} when the current
 * reading does not indicate the target chemical, {@code gasD} when it does.
 */
public enum Status {
    noGas,
    gasD
}
