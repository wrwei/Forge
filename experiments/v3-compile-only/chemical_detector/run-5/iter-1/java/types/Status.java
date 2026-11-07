package chemical_detector.types;

/** Outcome of one gas-analysis cycle (CD-DM1). */
public enum Status {
    /** The current reading does not indicate the target chemical. */
    noGas,
    /** The current reading indicates the target chemical is present. */
    gasD
}
