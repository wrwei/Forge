package chemdetector.datamodel;

/**
 * Outcome of a single gas-analysis cycle: the current reading either does
 * not indicate the target chemical (noGas) or does (gasD).
 */
public enum Status {
    noGas,
    gasD
}
