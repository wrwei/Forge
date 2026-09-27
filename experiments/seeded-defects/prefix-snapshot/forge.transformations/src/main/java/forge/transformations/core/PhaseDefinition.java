package forge.transformations.core;

import java.util.List;
import java.util.Map;

/**
 * Immutable view of one phase entry from pipeline.yaml. Only fields
 * the Java side reads are exposed; Python-only fields (e.g.
 * runner.function, timeout) are discarded silently at load time.
 */
public record PhaseDefinition(
    String id,
    String label,
    String runnerKind,
    String javaClass,           // null if kind != "java"
    Map<String, Object> args,
    List<String> dependsOn
) {}
