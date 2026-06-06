package chemdetector.datatype;

/**
 * CD-DM6: a single sensor record carrying chemical identity and measured intensity.
 */
public record GasSensor(Chem c, Intensity i) {
}
