package chemdetector.data;

/**
 * Outcome of one gas-analysis cycle (CD-DM1).
 * <ul>
 *   <li>{@link #noGas} — current reading does not indicate the target chemical</li>
 *   <li>{@link #gasD} — current reading indicates the target chemical is present</li>
 * </ul>
 */
public enum Status {
    noGas,
    gasD
}
