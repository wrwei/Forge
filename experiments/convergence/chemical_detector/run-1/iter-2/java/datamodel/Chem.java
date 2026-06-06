package chemdetector.datamodel;

/**
 * Identity of a chemical species (CD-DM4). Opaque: equality is the only
 * operation used. TARGET is the chemical the robot searches for; OTHER
 * stands for any non-target species.
 */
public enum Chem {
    TARGET,
    OTHER
}
