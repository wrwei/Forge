package chemdetector.datamodel;

/**
 * Identity of a chemical species (CD-DM4). Opaque — only equality is
 * needed. Modelled as a small enumeration: {@code TARGET} is the species
 * the mission is searching for, {@code OTHER} stands for any other
 * detected species.
 */
public enum Chem {
    TARGET,
    OTHER
}
