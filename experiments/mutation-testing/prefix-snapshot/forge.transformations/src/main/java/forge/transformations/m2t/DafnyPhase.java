package forge.transformations.m2t;

import forge.transformations.t2m.SpoonDiscoverer;
import forge.transformations.m2t.Java2DafnyEglTransformer;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import forge.transformations.core.Phase;
import forge.transformations.core.PhaseContext;
import forge.transformations.t2m.T2mPhase;

/**
 * Phase: Java EMF → Dafny (EGL transformation).
 *
 * <p>Reads the Spoon resource from context (populated by {@link T2mPhase}) or
 * runs discovery fresh if the context is empty. Generates a Dafny verification
 * file named {@code <ControllerClass>.dfy} and a traceability file
 * {@code trace_dafny.json} in the output directory.
 */
public final class DafnyPhase implements Phase {

    @Override
    @SuppressWarnings("unchecked")
    public void run(PhaseContext ctx) throws Exception {
        Path sourcePath = ctx.argPath("source");
        Path outputDir = ctx.argPath("output");
        Files.createDirectories(outputDir);

        // ── Obtain Spoon resource + discoverer metadata ──────────────────────
        Resource javaResource;
        Map<String, List<Map<String, Object>>> sourcePositionsByName;
        Map<EObject, Object> resolvedValues;

        SpoonDiscoverer discoverer = new SpoonDiscoverer();

        if (ctx.has("java_resource")) {
            javaResource = ctx.require("java_resource", Resource.class);
            sourcePositionsByName = (Map<String, List<Map<String, Object>>>) ctx.require("source_positions_by_name", Map.class);
            resolvedValues = (Map<EObject, Object>) ctx.require("resolved_values", Map.class);
        } else {
            // Standalone invocation: run discovery first and save artefacts.
            System.out.println("Discovering Java sources in: " + sourcePath);
            javaResource = discoverer.discover(sourcePath);

            Path xmiPath = outputDir.resolve("discovered_model.xmi");
            javaResource.setURI(URI.createFileURI(xmiPath.toAbsolutePath().toString()));
            javaResource.save(null);
            System.out.println("Model saved to: " + xmiPath.toAbsolutePath());
            discoverer.writeTraceFile(outputDir);

            sourcePositionsByName = discoverer.getSourcePositionsByName();
            resolvedValues = discoverer.getResolvedValues();

            // Store for downstream phases.
            ctx.put("java_resource", javaResource);
            ctx.put("source_positions", discoverer.getSourcePositionMap());
            ctx.put("source_positions_by_name", sourcePositionsByName);
            ctx.put("resolved_values", resolvedValues);
            ctx.put("record_metadata", discoverer.getRecordMetadata());
            ctx.put("constant_defaults", discoverer.getConstantDefaults());
        }

        // ── Discover controller class name ────────────────────────────────────
        String controllerName = discoverControllerName(javaResource);
        Path dafnyOutputPath = outputDir.resolve(controllerName + ".dfy");

        // ── Dafny EGL generation ──────────────────────────────────────────────
        System.out.println("Generating Dafny verification code (EGL)...");
        Java2DafnyEglTransformer dafnyTransformer = new Java2DafnyEglTransformer();
        dafnyTransformer.setResolvedValues(resolvedValues);
        dafnyTransformer.transform(javaResource, dafnyOutputPath);
        System.out.println("Dafny written to: " + dafnyOutputPath.toAbsolutePath());

        // ── Write Dafny trace ─────────────────────────────────────────────────
        writeDafnyTrace(dafnyTransformer.getTraceEntries(), outputDir,
                sourcePositionsByName, controllerName);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Find the controller class name: the CtClass that contains a step() method. */
    private static String discoverControllerName(Resource spoonResource) {
        for (var it = spoonResource.getAllContents(); it.hasNext(); ) {
            EObject obj = it.next();
            if ("CtClass".equals(obj.eClass().getName())) {
                for (EObject tm : obj.eContents()) {
                    if ("CtMethod".equals(tm.eClass().getName())) {
                        EStructuralFeature nameFeature = tm.eClass().getEStructuralFeature("name");
                        if (nameFeature != null && "step".equals(tm.eGet(nameFeature))) {
                            EStructuralFeature classNameFeature = obj.eClass().getEStructuralFeature("name");
                            if (classNameFeature != null) {
                                Object name = obj.eGet(classNameFeature);
                                if (name != null) return name.toString();
                            }
                        }
                    }
                }
            }
        }
        return "Controller";
    }

    /**
     * Write trace_dafny.json from EGL-produced trace entries, enriched with
     * Java source positions from the Spoon discovery map.
     */
    private static void writeDafnyTrace(List<Map<String, String>> eglTraceEntries, Path outputDir,
                                        Map<String, List<Map<String, Object>>> javaPositions,
                                        String controllerName) {
        if (eglTraceEntries == null || eglTraceEntries.isEmpty()) return;

        List<Map<String, Object>> entries = new ArrayList<>();
        for (Map<String, String> raw : eglTraceEntries) {
            Map<String, Object> entry = new LinkedHashMap<>(raw);

            String dafnyType = raw.get("dafny_type");
            String dafnyElement = raw.get("dafny_element");

            if (javaPositions != null && dafnyElement != null) {
                if ("TransitionMethod".equals(dafnyType) || "StepMethod".equals(dafnyType)
                        || "Constructor".equals(dafnyType)) {
                    enrichWithJavaSource(entry, "step", javaPositions, "CtMethod");
                    if (!entry.containsKey("java_file")) {
                        enrichWithJavaSource(entry, controllerName, javaPositions, "CtClass");
                    }
                } else if ("ModeDatatype".equals(dafnyType)) {
                    enrichWithJavaSource(entry, dafnyElement, javaPositions, "CtEnum");
                } else if ("AbstractFunction".equals(dafnyType)) {
                    enrichWithJavaSource(entry, dafnyElement, javaPositions, "CtMethod");
                    if (!entry.containsKey("java_file")) {
                        enrichWithJavaSource(entry, dafnyElement, javaPositions, "CtField");
                    }
                } else if ("Lemma".equals(dafnyType)) {
                    enrichWithJavaSource(entry, "step", javaPositions, "CtMethod");
                } else {
                    enrichWithJavaSource(entry, dafnyElement, javaPositions, null);
                }
            }
            entries.add(entry);
        }

        Path outPath = outputDir.resolve("trace_dafny.json");
        try (Writer w = Files.newBufferedWriter(outPath)) {
            w.write("{\n  \"mappings\": [\n");
            for (int i = 0; i < entries.size(); i++) {
                Map<String, Object> entry = entries.get(i);
                w.write("    {");
                boolean first = true;
                for (Map.Entry<String, Object> kv : entry.entrySet()) {
                    if (!first) w.write(", ");
                    if (kv.getValue() instanceof Integer || kv.getValue() instanceof Long) {
                        w.write("\"" + kv.getKey() + "\": " + kv.getValue());
                    } else {
                        w.write("\"" + kv.getKey() + "\": \""
                                + escapeJson(String.valueOf(kv.getValue())) + "\"");
                    }
                    first = false;
                }
                w.write("}");
                if (i < entries.size() - 1) w.write(",");
                w.write("\n");
            }
            w.write("  ]\n}\n");
            System.out.println("Dafny trace written to: " + outPath.toAbsolutePath()
                    + " (" + entries.size() + " entries)");
        } catch (IOException ex) {
            System.err.println("Warning: Failed to write Dafny trace: " + ex.getMessage());
        }
    }

    private static void enrichWithJavaSource(Map<String, Object> entry, String simpleName,
                                             Map<String, List<Map<String, Object>>> javaPositions,
                                             String preferredType) {
        List<Map<String, Object>> candidates = javaPositions.get(simpleName);
        if (candidates == null || candidates.isEmpty()) return;

        Map<String, Object> best = null;
        for (Map<String, Object> c : candidates) {
            String emfType = (String) c.get("emf_type");
            if (preferredType != null && preferredType.equals(emfType)) {
                best = c;
                break;
            }
        }
        if (best == null) {
            best = candidates.get(0);
        }

        entry.put("java_qualified_name", best.get("qualified_name"));
        entry.put("java_file", best.get("file"));
        entry.put("java_line_start", best.get("line_start"));
        entry.put("java_line_end", best.get("line_end"));
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
