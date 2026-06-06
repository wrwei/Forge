package chemdetector.datatype;

/**
 * A single sensor reading: chemical identity c and intensity i.
 */
public record GasSensor(Chem c, Intensity i) {
}
