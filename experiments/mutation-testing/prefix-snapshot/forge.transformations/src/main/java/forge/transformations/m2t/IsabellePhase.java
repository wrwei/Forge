package forge.transformations.m2t;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import forge.transformations.core.IsabelleEgxRunner;
import forge.transformations.m2m.RoboChartMetamodel;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forge.transformations.core.Phase;
import forge.transformations.core.PhaseContext;
import forge.transformations.m2m.TransformPhase;

/**
 * Phase: RoboChart EMF → Isabelle/UTP Z-Machine theory (.thy).
 *
 * <p>Reads the RoboChart resource from context (populated by {@link TransformPhase})
 * or loads {@code robochart_model.xmi} from disk. Writes
 * {@code <output>/isabelle/<StmName>_Beh.thy}, {@code <output>/isabelle/ROOT}, and
 * {@code <output>/trace_isabelle.json} (Z-Machine construct → .thy line).
 */
public final class IsabellePhase implements Phase {

    /** A Z-Machine operation declaration: {@code zoperation <Name> = ...}. */
    private static final Pattern ZOPERATION = Pattern.compile("^\\s*zoperation\\s+(\\w+)");
    /** A named lemma: {@code lemma <Name> [..]:} or {@code lemma <Name>:}. */
    private static final Pattern NAMED_LEMMA = Pattern.compile("^\\s*lemma\\s+(\\w+)\\s*(?:\\[|:)");

    @Override
    public void run(PhaseContext ctx) throws Exception {
        Path outputDir = ctx.argPath("output");
        Files.createDirectories(outputDir);

        Resource rcResource;
        if (ctx.has("rc_resource")) {
            rcResource = ctx.require("rc_resource", Resource.class);
        } else {
            rcResource = loadRoboChartModel(outputDir);
        }

        System.out.println("Generating Isabelle Z-Machine theory...");
        Path thyPath;
        try {
            IsabelleEgxRunner runner = new IsabelleEgxRunner();
            thyPath = runner.run(rcResource, RoboChartMetamodel.getInstance().ePackage(), outputDir);
        } catch (Exception e) {
            throw new IOException("Isabelle theory generation failed: " + e.getMessage(), e);
        }

        // ── Write trace (zoperations + named lemmas → .thy line) ─────────────
        writeIsabelleTrace(thyPath, outputDir);
    }

    private static Resource loadRoboChartModel(Path outputDir) throws IOException {
        Path xmiPath = outputDir.resolve("robochart_model.xmi");
        if (!Files.exists(xmiPath)) {
            throw new IOException("robochart_model.xmi not found at "
                    + xmiPath.toAbsolutePath() + ". Run 'transform' first.");
        }
        RoboChartMetamodel.getInstance();
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("xmi", new XMIResourceFactoryImpl());
        Resource resource = rs.getResource(
                URI.createFileURI(xmiPath.toAbsolutePath().toString()), true);
        System.out.println("Loaded RoboChart model from: " + xmiPath.toAbsolutePath());
        return resource;
    }

    /**
     * Parse the generated {@code .thy} and emit {@code trace_isabelle.json},
     * mapping each Z-Machine construct (zoperation, named lemma) to its 1-based
     * line in the theory. Mirrors the per-phase trace files written by the RCT
     * and Dafny phases, so the trace consolidator can attach Isabelle evidence
     * to the RoboChart elements — and thence the requirements — that produced it.
     */
    private static void writeIsabelleTrace(Path thyPath, Path outputDir) {
        if (thyPath == null || !Files.exists(thyPath)) return;

        List<Map<String, Object>> entries = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(thyPath);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                Matcher zop = ZOPERATION.matcher(line);
                if (zop.find()) {
                    entries.add(traceEntry(zop.group(1), "zoperation", i + 1));
                    continue;
                }
                Matcher lem = NAMED_LEMMA.matcher(line);
                if (lem.find()) {
                    entries.add(traceEntry(lem.group(1), "lemma", i + 1));
                }
            }
        } catch (IOException ex) {
            System.err.println("Warning: Failed to read theory for trace: " + ex.getMessage());
            return;
        }
        if (entries.isEmpty()) return;

        Path outPath = outputDir.resolve("trace_isabelle.json");
        try (Writer w = Files.newBufferedWriter(outPath)) {
            w.write("{\n  \"mappings\": [\n");
            for (int i = 0; i < entries.size(); i++) {
                writeEntry(w, entries.get(i));
                if (i < entries.size() - 1) w.write(",");
                w.write("\n");
            }
            w.write("  ]\n}\n");
            System.out.println("Isabelle trace written to: " + outPath.toAbsolutePath()
                    + " (" + entries.size() + " entries)");
        } catch (IOException ex) {
            System.err.println("Warning: Failed to write Isabelle trace: " + ex.getMessage());
        }
    }

    private static Map<String, Object> traceEntry(String element, String type, int line) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("isabelle_element", element);
        e.put("isabelle_type", type);
        e.put("thy_line_start", line);
        return e;
    }

    private static void writeEntry(Writer w, Map<String, Object> entry) throws IOException {
        w.write("    {");
        boolean first = true;
        for (Map.Entry<String, Object> kv : entry.entrySet()) {
            if (!first) w.write(", ");
            Object v = kv.getValue();
            if (v instanceof Integer || v instanceof Long) {
                w.write("\"" + kv.getKey() + "\": " + v);
            } else {
                w.write("\"" + kv.getKey() + "\": \"" + escapeJson(String.valueOf(v)) + "\"");
            }
            first = false;
        }
        w.write("}");
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
