package forge.transformations.core;

import org.yaml.snakeyaml.Yaml;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class Manifest {
    private final Path outputDir;
    private final Map<String, PhaseDefinition> phases;

    private Manifest(Path outputDir, Map<String, PhaseDefinition> phases) {
        this.outputDir = outputDir;
        this.phases = Map.copyOf(phases);
    }

    public static Manifest load(Path manifestFile) throws IOException {
        try (Reader r = Files.newBufferedReader(manifestFile)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> raw = new Yaml().load(r);
            return parse(raw, manifestFile.toAbsolutePath().getParent());
        }
    }

    @SuppressWarnings("unchecked")
    private static Manifest parse(Map<String, Object> raw, Path repoRoot) {
        Map<String, String> topVars = new LinkedHashMap<>();
        topVars.put("repo", repoRoot.toString());
        for (var e : raw.entrySet()) {
            if (e.getValue() instanceof String s) topVars.put(e.getKey(), resolveVars(s, topVars));
        }

        Path outputDir = topVars.containsKey("output_dir")
                ? Path.of(topVars.get("output_dir")) : repoRoot;

        Map<String, Object> phasesRaw = (Map<String, Object>) raw.get("phases");
        if (phasesRaw == null) throw new IllegalArgumentException("Manifest missing 'phases'");

        Map<String, PhaseDefinition> phases = new LinkedHashMap<>();
        for (var entry : phasesRaw.entrySet()) {
            String id = entry.getKey();
            Map<String, Object> body = (Map<String, Object>) entry.getValue();
            phases.put(id, parsePhase(id, body, topVars));
        }
        validateDependsOn(phases);
        return new Manifest(outputDir, phases);
    }

    @SuppressWarnings("unchecked")
    private static PhaseDefinition parsePhase(String id, Map<String, Object> body,
                                              Map<String, String> topVars) {
        String label = (String) body.get("label");
        if (label == null) throw new IllegalArgumentException("Phase " + id + " missing 'label'");

        Map<String, Object> runner = (Map<String, Object>) body.get("runner");
        if (runner == null) throw new IllegalArgumentException("Phase " + id + " missing 'runner'");
        String kind = (String) runner.get("kind");
        String javaClass = "java".equals(kind) ? (String) runner.get("class") : null;

        Map<String, Object> args = new LinkedHashMap<>();
        Map<String, Object> argsRaw = (Map<String, Object>) body.getOrDefault("args", Map.of());
        for (var e : argsRaw.entrySet()) {
            args.put(e.getKey(),
                    e.getValue() instanceof String s ? resolveVars(s, topVars) : e.getValue());
        }

        List<String> dependsOn = (List<String>) body.getOrDefault("depends_on", List.of());
        return new PhaseDefinition(id, label, kind, javaClass, args, List.copyOf(dependsOn));
    }

    private static String resolveVars(String s, Map<String, String> vars) {
        String out = s;
        for (var e : vars.entrySet()) {
            out = out.replace("${" + e.getKey() + "}", e.getValue());
        }
        if (out.contains("${")) {
            throw new IllegalArgumentException("Unresolved variable in: " + s);
        }
        return out;
    }

    private static void validateDependsOn(Map<String, PhaseDefinition> phases) {
        for (PhaseDefinition p : phases.values()) {
            for (String dep : p.dependsOn()) {
                if (!phases.containsKey(dep)) {
                    throw new IllegalArgumentException(
                        "Phase '" + p.id() + "' depends on unknown phase '" + dep + "'");
                }
            }
        }
        // Reject dependency cycles: a cycle would make any topological
        // ordering of the phases (dashboard-side Manifest.ordered(), or a
        // human following depends_on) silently wrong rather than failing
        // fast at load time. Iterative DFS with three colours.
        Map<String, Integer> colour = new HashMap<>();  // 0/absent=white, 1=grey, 2=black
        for (String start : phases.keySet()) {
            if (colour.getOrDefault(start, 0) != 0) continue;
            Deque<String> stack = new ArrayDeque<>();
            stack.push(start);
            while (!stack.isEmpty()) {
                String id = stack.peek();
                int c = colour.getOrDefault(id, 0);
                if (c == 0) {
                    colour.put(id, 1);
                    for (String dep : phases.get(id).dependsOn()) {
                        int dc = colour.getOrDefault(dep, 0);
                        if (dc == 1) {
                            throw new IllegalArgumentException(
                                "Dependency cycle involving phase '" + dep
                                + "' (reached again from '" + id + "')");
                        }
                        if (dc == 0) stack.push(dep);
                    }
                } else {
                    colour.put(id, 2);
                    stack.pop();
                }
            }
        }
    }

    public Path outputDir() { return outputDir; }

    public PhaseDefinition phase(String id) {
        PhaseDefinition p = phases.get(id);
        if (p == null) throw new IllegalArgumentException("Unknown phase: " + id);
        return p;
    }

    public Set<String> phaseIds() { return phases.keySet(); }
}
