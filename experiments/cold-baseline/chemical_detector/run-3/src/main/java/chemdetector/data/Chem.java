package chemdetector.data;

/**
 * CD-DM4: Chem is an opaque type representing the identity of a
 * chemical species. Equality is the only operation needed; the
 * record wraps a single integer identifier.
 */
public record Chem(int id) {
}
