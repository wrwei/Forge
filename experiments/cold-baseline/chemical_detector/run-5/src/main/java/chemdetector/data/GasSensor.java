package chemdetector.data;

/**
 * CD-DM6 — Single gas-sensor reading: chemical identity c and intensity i.
 */
public record GasSensor(Chem c, Intensity i) {
}
