package chemical_detector.data;

/**
 * Identity of a chemical species (CD-DM4). Equality is the only operation
 * the system needs, so the type is modelled as a closed enumeration that
 * distinguishes the mission's target chemical from anything else.
 */
public enum Chem {
    Target,
    Other
}
