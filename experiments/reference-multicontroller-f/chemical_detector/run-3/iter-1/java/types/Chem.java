package chemical_detector.types;

/**
 * Identity of a chemical species. Only equality is used; the detector distinguishes the
 * target chemical from every other species.
 */
public enum Chem {
    TARGET,
    OTHER
}
