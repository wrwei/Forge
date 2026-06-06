package chemdetector.data;

/**
 * CD-DM6: A single sensor's reading: a chemical identity {@code c}
 * paired with its measured intensity {@code i}.
 */
public record GasSensor(Chem c, Intensity i) {
}
