package chemical_detector.domain;

/**
 * Outcome of a single gas-analysis cycle.
 */
public enum Status {
    /** The current reading does not indicate the target chemical. */
    noGas,
    /** The current reading indicates that the target chemical is present. */
    gasD
}
