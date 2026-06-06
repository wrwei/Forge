package chemdetector.data;

import chemdetector.annotation.RoboChartType;

/**
 * CD-DM4: Opaque identity of a chemical species. Equality is the only
 * operation needed; we represent it as a wrapper around a natural
 * number so RoboChart sees a primitive {@code nat}.
 */
public record Chem(@RoboChartType("nat") int id) {
}
