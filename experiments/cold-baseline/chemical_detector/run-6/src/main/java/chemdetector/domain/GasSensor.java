package chemdetector.domain;

/**
 * A single sensor reading: chemical identity {@code c} and measured
 * intensity {@code i} (CD-DM6).
 */
public record GasSensor(Chem c, Intensity i) {
}
