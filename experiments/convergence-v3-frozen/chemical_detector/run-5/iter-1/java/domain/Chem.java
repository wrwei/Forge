package chemical_detector.domain;

/**
 * Identity of a chemical species reported by a gas sensor. Only equality is
 * used on it: the analysis distinguishes the species being searched for from
 * any other species the sensor array can report.
 */
public enum Chem {
    /** The chemical species the detector is searching for. */
    TARGET,
    /** Any other species the sensor array can report. */
    OTHER
}
