package chemdetector.datatype;

/**
 * CD-DM6: a single (chemical, intensity) pair from one sensor.
 */
public record GasSensor(Chem c, Intensity i) {
}
