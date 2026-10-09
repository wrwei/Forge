package forge.transformations.core;

import forge.transformations.m2t.RctPhase;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
// Same-package classes (Manifest, PhaseContext, PhaseDefinition, PhaseRegistry)
// do not require explicit imports.

/**
 * Pipeline entry point. Loads pipeline.yaml, finds the requested phase,
 * dispatches to its kind:java Phase implementation. Sequence, gradle, and
 * python phases are NOT handled here — the dashboard layer expands them.
 *
 * Usage: t2m <phase-id> [arg=value ...]
 *
 * Examples:
 *   t2m t2m         source=src/main/java output=output
 *   t2m m2m         source=src/main/java output=output
 *   t2m isabelle_gen output=output
 *
 * As a fallback for sequence inner steps, accepts a fully-qualified
 * Phase class name in place of a phase id:
 *   t2m forge.transformations.m2t.RctPhase output=output
 */
public class App {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) { printUsage(); System.exit(1); }

        String first = args[0];
        Map<String, Object> mergedArgs = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            String kv = args[i];
            int eq = kv.indexOf('=');
            if (eq < 0) continue;
            mergedArgs.put(kv.substring(0, eq), kv.substring(eq + 1));
        }

        Phase phase;
        Path outputDir;

        if (first.contains(".")) {
            // Class FQN form (for sequence inner steps the dashboard expands).
            phase = PhaseRegistry.load(first);
            outputDir = mergedArgs.containsKey("output")
                    ? Path.of(mergedArgs.get("output").toString())
                    : Path.of("output");
        } else {
            // Phase id form: look up in manifest.
            Manifest manifest = Manifest.load(findManifest());
            PhaseDefinition def = manifest.phase(first);
            if (!"java".equals(def.runnerKind())) {
                System.err.println("App.java only dispatches kind=java phases; '"
                        + first + "' is kind=" + def.runnerKind());
                System.exit(2);
                return;
            }
            // Merge manifest args with CLI args; CLI wins.
            Map<String, Object> manifestArgs = new HashMap<>(def.args());
            manifestArgs.putAll(mergedArgs);
            mergedArgs = manifestArgs;
            phase = PhaseRegistry.load(def.javaClass());
            outputDir = manifest.outputDir();
        }

        PhaseContext ctx = new PhaseContext(outputDir, mergedArgs);
        phase.run(ctx);
    }

    /** Walk up from CWD until pipeline.yaml is found; throw if not found. */
    private static Path findManifest() {
        Path p = Path.of("").toAbsolutePath();
        while (p != null) {
            Path candidate = p.resolve("pipeline.yaml");
            if (Files.exists(candidate)) return candidate;
            p = p.getParent();
        }
        throw new IllegalStateException("pipeline.yaml not found");
    }

    private static void printUsage() {
        System.err.println("Usage: t2m <phase-id> [arg=value ...]");
        System.err.println("       t2m <Phase-class-FQN> [arg=value ...]");
        System.err.println();
        System.err.println("Run a phase by id from pipeline.yaml, or by FQN class name");
        System.err.println("(useful for sequence inner steps).");
    }
}
