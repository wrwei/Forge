package chemical_detector.data;

/**
 * Opaque identity of a chemical species (CD-DM4); equality is the only operation used.
 * The controller only needs to tell the target chemical apart from any other species.
 */
public enum Chem {
    TARGET,
    OTHER
}
