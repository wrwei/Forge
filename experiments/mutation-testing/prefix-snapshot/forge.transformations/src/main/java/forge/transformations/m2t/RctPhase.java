package forge.transformations.m2t;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import forge.transformations.m2t.RoboChart2RctTransformer;
import forge.transformations.m2m.RoboChartMetamodel;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.transformations.core.Phase;
import forge.transformations.core.PhaseContext;
import forge.transformations.m2m.TransformPhase;

/**
 * Phase: RoboChart EMF → RoboChart textual notation (.rct).
 *
 * <p>Reads the RoboChart resource from context (populated by {@link TransformPhase})
 * or loads {@code robochart_model.xmi} from disk. Applies optional constant defaults,
 * writes {@code robochart_controller.rct}, and emits {@code trace_m2t_rct.json}.
 */
public final class RctPhase implements Phase {

    @Override
    public void run(PhaseContext ctx) throws Exception {
        Path outputDir = ctx.argPath("output");
        Files.createDirectories(outputDir);

        // ── Obtain RoboChart resource ────────────────────────────────────────
        Resource rcResource;
        if (ctx.has("rc_resource")) {
            rcResource = ctx.require("rc_resource", Resource.class);
        } else {
            rcResource = loadRoboChartModel(outputDir);
        }

        // ── Set up transformer ───────────────────────────────────────────────
        System.out.println("Generating RoboChart textual notation...");
        RoboChart2RctTransformer rctTransformer = new RoboChart2RctTransformer();

        Path constPath = outputDir.resolve("constant_defaults.json");
        if (Files.exists(constPath)) {
            Map<String, Object> defaults = loadConstantDefaults(constPath);
            rctTransformer.setConstantDefaults(defaults);
        }

        // ── Transform ────────────────────────────────────────────────────────
        Path rctOutputPath = outputDir.resolve("robochart_controller.rct");
        rctTransformer.transform(rcResource, rctOutputPath);
        System.out.println("RCT written to: " + rctOutputPath.toAbsolutePath());

        // ── Write trace ──────────────────────────────────────────────────────
        writeRctTrace(rctTransformer.getTraceEntries(), outputDir);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

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

    private static Object parseConstantDefaultValue(String raw) {
        String s = raw.trim();
        if (s.isEmpty() || s.equals("null")) return null;
        if (s.equals("true")) return Boolean.TRUE;
        if (s.equals("false")) return Boolean.FALSE;
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            String body = s.substring(1, s.length() - 1);
            return body.replace("\\\"", "\"").replace("\\\\", "\\")
                    .replace("\\n", "\n").replace("\\r", "\r");
        }
        try { return Long.parseLong(s); } catch (NumberFormatException ignore) { /* fall through */ }
        try { return Double.parseDouble(s); } catch (NumberFormatException ignore) { /* fall through */ }
        return s;
    }

    private static Map<String, Object> loadConstantDefaults(Path path) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String content = Files.readString(path);
        for (String line : content.split("\n")) {
            line = line.trim();
            if (!line.startsWith("\"")) continue;
            int closeQuote = line.indexOf('"', 1);
            if (closeQuote < 0) continue;
            int colonIdx = line.indexOf(':', closeQuote);
            if (colonIdx < 0) continue;
            String key = line.substring(1, closeQuote);
            String val = line.substring(colonIdx + 1).trim();
            if (val.endsWith(",")) val = val.substring(0, val.length() - 1).trim();
            result.put(key, parseConstantDefaultValue(val));
        }
        return result;
    }

    private static void writeRctTrace(List<Map<String, String>> eglTraceEntries, Path outputDir) {
        if (eglTraceEntries == null || eglTraceEntries.isEmpty()) return;

        Path outPath = outputDir.resolve("trace_m2t_rct.json");
        try (Writer w = Files.newBufferedWriter(outPath)) {
            w.write("{\n  \"mappings\": [\n");
            for (int i = 0; i < eglTraceEntries.size(); i++) {
                Map<String, String> entry = eglTraceEntries.get(i);
                w.write("    {");
                boolean first = true;
                for (Map.Entry<String, String> kv : entry.entrySet()) {
                    if (!first) w.write(", ");
                    String val = String.valueOf(kv.getValue());
                    try {
                        int numVal = Integer.parseInt(val);
                        w.write("\"" + kv.getKey() + "\": " + numVal);
                    } catch (NumberFormatException e) {
                        w.write("\"" + kv.getKey() + "\": \"" + escapeJson(val) + "\"");
                    }
                    first = false;
                }
                w.write("}");
                if (i < eglTraceEntries.size() - 1) w.write(",");
                w.write("\n");
            }
            w.write("  ]\n}\n");
            System.out.println("RCT trace written to: " + outPath.toAbsolutePath()
                    + " (" + eglTraceEntries.size() + " entries)");
        } catch (IOException ex) {
            System.err.println("Warning: Failed to write RCT trace: " + ex.getMessage());
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
