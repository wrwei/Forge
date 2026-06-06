package chemdetector.domain;

/**
 * Opaque chemical-species identity (CD-DM4). Equality is the only
 * required operation. Represented as a String tag for runtime
 * convenience; only equality is used by the controllers.
 */
public record Chem(String id) {
}
