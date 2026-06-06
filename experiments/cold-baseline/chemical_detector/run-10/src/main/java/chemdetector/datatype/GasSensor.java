package chemdetector.datatype;

/**
 * CD-DM6. GasSensor record: one sensor reading with chemical identity and
 * measured intensity.
 */
public record GasSensor(Chem c, Intensity i) {
}
