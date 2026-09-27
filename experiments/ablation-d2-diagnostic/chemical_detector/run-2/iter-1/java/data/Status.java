package chemical_detector.data;

/**
 * Outcome of a single gas-analysis cycle: the current reading either does not
 * indicate the target chemical ({@code noGas}) or does ({@code gasD}).
 */
public enum Status {
    noGas,
    gasD
}
