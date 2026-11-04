package chemical_detector;

/**
 * The outcome of a single gas-analysis cycle (CD-DM1).
 */
public enum Status {
    /** The current reading does not indicate the target chemical. */
    noGas,
    /** The current reading indicates the target chemical is present. */
    gasD
}
