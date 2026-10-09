package forge.transformations.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * In-JVM blackboard for phase chaining. Phases put intermediate results
 * into the context for downstream phases to consume. When invoked
 * standalone (each phase in its own JVM), the context starts empty and
 * each phase must load what it needs from disk.
 */
public final class PhaseContext {
    private final Map<String, Object> bag = new HashMap<>();
    private final Map<String, Object> args;
    private final Path outputDir;

    public PhaseContext(Path outputDir, Map<String, Object> args) {
        this.outputDir = outputDir;
        this.args = Map.copyOf(args);
    }

    public Path outputDir() { return outputDir; }

    public Path argPath(String name) {
        Object v = args.get(name);
        if (v == null) throw new IllegalStateException("Missing arg: " + name);
        return Paths.get(v.toString());
    }

    public String argString(String name) {
        Object v = args.get(name);
        if (v == null) throw new IllegalStateException("Missing arg: " + name);
        return v.toString();
    }

    public <T> T require(String key, Class<T> type) {
        Object v = bag.get(key);
        if (v == null) throw new IllegalStateException("PhaseContext missing: " + key);
        return type.cast(v);
    }

    public void put(String key, Object value) { bag.put(key, value); }
    public boolean has(String key) { return bag.containsKey(key); }
}
