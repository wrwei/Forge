package chemical_detector.domain;

/** Outcome of classifying one gas reading. */
public enum Status {
    /** The reading does not indicate the target chemical. */
    noGas,
    /** The reading indicates the target chemical is present. */
    gasD
}
