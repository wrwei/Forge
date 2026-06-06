package forge.transformations.core;

/**
 * Loads a Phase implementation by fully-qualified class name. Throws
 * with a clear message if the class is missing, not a Phase, or has
 * no no-arg constructor. Used by App.java to dispatch a manifest entry
 * (kind: java, class: ...) to its implementation.
 */
public final class PhaseRegistry {
    private PhaseRegistry() {}

    public static Phase load(String className) {
        try {
            Class<?> cls = Class.forName(className);
            Object instance = cls.getDeclaredConstructor().newInstance();
            if (!(instance instanceof Phase phase)) {
                throw new IllegalStateException(
                    className + " does not implement " + Phase.class.getName());
            }
            return phase;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot load phase: " + className, e);
        }
    }
}
