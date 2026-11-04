package chemical_detector.domain;

/**
 * Outcome of one gas-analysis cycle.
 */
public enum Status {
    /** The current reading does not indicate the target chemical. */
    noGas,
    /** The current reading indicates the target chemical is present. */
    gasD
}
