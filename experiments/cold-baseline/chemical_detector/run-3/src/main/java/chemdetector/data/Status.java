package chemdetector.data;

/**
 * CD-DM1: Status enumeration captures the outcome of a single
 * gas-analysis cycle. noGas = current reading does not indicate
 * the target chemical. gasD = current reading indicates the
 * target chemical is present.
 */
public enum Status {
    noGas,
    gasD
}
