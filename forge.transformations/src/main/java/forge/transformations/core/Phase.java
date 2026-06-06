package forge.transformations.core;

/**
 * A pipeline phase. Each phase reads zero or more artifacts from the
 * context (or loads them from disk), performs its transformation, and
 * writes any new artifacts back into the context for downstream phases.
 */
public interface Phase {
    void run(PhaseContext ctx) throws Exception;
}
