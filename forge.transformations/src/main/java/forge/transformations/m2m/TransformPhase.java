package forge.transformations.m2m;

import forge.transformations.t2m.RecordMetadataResolver;
import forge.transformations.t2m.SpoonDiscoverer;
import forge.transformations.m2m.Java2RoboChartTransformer;
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
 * Phase: Java EMF → RoboChart EMF (ETL transformation).
 *
 * <p>Reads the Spoon resource from context (populated by {@link T2mPhase}) or
 * runs discovery fresh if the context is empty. Applies the ETL transform,
 * canonicalizes duplicate PrimitiveType instances, saves
 * {@code robochart_model.xmi} and {@code trace_m2m.json}, and stores the
 * resulting RoboChart resource under the {@code rc_resource} context key.
 */
public final class TransformPhase implements Phase {

    @Override
    @SuppressWarnings("unchecked")
    public void run(PhaseContext ctx) throws Exception {
        Path sourcePath = ctx.argPath("source");
        Path outputDir = ctx.argPath("output");
        Files.createDirectories(outputDir);

        // ── Obtain Spoon resource + discoverer metadata ─────────────────────
        Resource javaResource;
        Map<EObject, List<Integer>> sourcePositionMap;
        Map<String, List<Map<String, Object>>> sourcePositionsByName;
        Map<EObject, Object> resolvedValues;
        Map<String, List<RecordMetadataResolver.FieldInfo>> recordMetadata;
        Map<String, Object> constantDefaults;

        SpoonDiscoverer discoverer = new SpoonDiscoverer();

        if (ctx.has("java_resource")) {
            javaResource = ctx.require("java_resource", Resource.class);
            sourcePositionMap = (Map<EObject, List<Integer>>) ctx.require("source_positions", Map.class);
            sourcePositionsByName = (Map<String, List<Map<String, Object>>>) ctx.require("source_positions_by_name", Map.class);
            resolvedValues = (Map<EObject, Object>) ctx.require("resolved_values", Map.class);
            recordMetadata = (Map<String, List<RecordMetadataResolver.FieldInfo>>) ctx.require("record_metadata", Map.class);
            constantDefaults = (Map<String, Object>) ctx.require("constant_defaults", Map.class);
        } else {
            // Standalone invocation: run discovery first and save artefacts.
            System.out.println("Discovering Java sources in: " + sourcePath);
            javaResource = discoverer.discover(sourcePath);

            Path xmiPath = outputDir.resolve("discovered_model.xmi");
            javaResource.setURI(URI.createFileURI(xmiPath.toAbsolutePath().toString()));
            javaResource.save(null);
            System.out.println("Model saved to: " + xmiPath.toAbsolutePath());
            discoverer.writeTraceFile(outputDir);

            sourcePositionMap = discoverer.getSourcePositionMap();
            sourcePositionsByName = discoverer.getSourcePositionsByName();
            resolvedValues = discoverer.getResolvedValues();
            recordMetadata = discoverer.getRecordMetadata();
            constantDefaults = discoverer.getConstantDefaults();
        }

        // ── ETL: Spoon EMF → RoboChart EMF ──────────────────────────────────
        System.out.println("Extracting RoboChart state machine...");
        Java2RoboChartTransformer transformer = new Java2RoboChartTransformer();
        transformer.setSourcePositionMap(sourcePositionMap);
        Resource rcResource = transformer.transform(javaResource, resolvedValues, recordMetadata);

        // Post-ETL: deduplicate PrimitiveType instances (Isabelle rejects duplicates).
        int deduped = canonicalizePrimitiveTypes(rcResource);
        if (deduped > 0) {
            System.out.println("Canonicalized " + deduped + " duplicate PrimitiveType(s)");
        }

        // ── Save robochart_model.xmi ─────────────────────────────────────────
        Path rcOutputPath = outputDir.resolve("robochart_model.xmi");
        rcResource.setURI(URI.createFileURI(rcOutputPath.toAbsolutePath().toString()));
        rcResource.save(null);
        System.out.println("RoboChart model saved to: " + rcOutputPath.toAbsolutePath());

        // ── Save constant_defaults.json ──────────────────────────────────────
        if (!constantDefaults.isEmpty()) {
            Path constPath = outputDir.resolve("constant_defaults.json");
            try (Writer w = Files.newBufferedWriter(constPath)) {
                w.write("{\n");
                var entries = new ArrayList<>(constantDefaults.entrySet());
                for (int i = 0; i < entries.size(); i++) {
                    var e = (Map.Entry<String, Object>) entries.get(i);
                    w.write("  \"" + escapeJson(e.getKey()) + "\": " + jsonValueOf(e.getValue()));
                    if (i < entries.size() - 1) w.write(",");
                    w.write("\n");
                }
                w.write("}\n");
            }
        }

        // ── Write M2M trace ──────────────────────────────────────────────────
        writeM2MTrace(transformer.getTraceEntries(), outputDir, sourcePositionsByName);

        // ── Print summary ────────────────────────────────────────────────────
        printRoboChartSummary(rcResource);

        // ── Store in context ─────────────────────────────────────────────────
        ctx.put("rc_resource", rcResource);
        // Forward discovery results if this phase ran discovery standalone.
        if (!ctx.has("java_resource")) {
            ctx.put("java_resource", javaResource);
            ctx.put("source_positions", sourcePositionMap);
            ctx.put("source_positions_by_name", sourcePositionsByName);
            ctx.put("resolved_values", resolvedValues);
            ctx.put("record_metadata", recordMetadata);
            ctx.put("constant_defaults", constantDefaults);
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Canonicalize duplicate {@code PrimitiveType} instances inside an
     * {@code RCPackage}. The ETL emits a fresh instance per use; duplicates
     * cause downstream theory generation to emit repeated type synonyms that
     * Isabelle rejects.
     *
     * @return the number of duplicate instances removed
     */
    @SuppressWarnings("unchecked")
    private static int canonicalizePrimitiveTypes(Resource rcResource) {
        EObject rcPackage = null;
        for (EObject obj : rcResource.getContents()) {
            if ("RCPackage".equals(obj.eClass().getName())) {
                rcPackage = obj;
                break;
            }
        }
        if (rcPackage == null) return 0;

        EStructuralFeature typesFeature = rcPackage.eClass().getEStructuralFeature("types");
        if (typesFeature == null) return 0;
        List<EObject> types = (List<EObject>) rcPackage.eGet(typesFeature);

        // Pass 1: first instance per name is canonical; collect duplicates.
        Map<String, EObject> canonicalByName = new LinkedHashMap<>();
        List<EObject> duplicates = new ArrayList<>();
        for (EObject t : types) {
            if (!"PrimitiveType".equals(t.eClass().getName())) continue;
            EStructuralFeature nameFeature = t.eClass().getEStructuralFeature("name");
            if (nameFeature == null) continue;
            Object nameObj = t.eGet(nameFeature);
            if (nameObj == null) continue;
            String name = nameObj.toString();
            if (canonicalByName.putIfAbsent(name, t) != null) {
                duplicates.add(t);
            }
        }
        if (duplicates.isEmpty()) return 0;

        // Pass 2: repoint any TypeRef.ref that aimed at a duplicate.
        for (var it = rcResource.getAllContents(); it.hasNext(); ) {
            EObject obj = it.next();
            if (!"TypeRef".equals(obj.eClass().getName())) continue;
            EStructuralFeature refFeature = obj.eClass().getEStructuralFeature("ref");
            if (refFeature == null) continue;
            Object refObj = obj.eGet(refFeature);
            if (!(refObj instanceof EObject ref)) continue;
            if (!"PrimitiveType".equals(ref.eClass().getName())) continue;
            if (!duplicates.contains(ref)) continue;
            EStructuralFeature nameFeature = ref.eClass().getEStructuralFeature("name");
            if (nameFeature == null) continue;
            Object nameObj = ref.eGet(nameFeature);
            if (nameObj == null) continue;
            EObject canonical = canonicalByName.get(nameObj.toString());
            if (canonical != null && canonical != ref) {
                obj.eSet(refFeature, canonical);
            }
        }

        // Pass 3: remove duplicates from the types list.
        types.removeAll(duplicates);
        return duplicates.size();
    }

    private static void writeM2MTrace(List<Map<String, String>> etlTraceEntries, Path outputDir,
                                      Map<String, List<Map<String, Object>>> javaPositions) {
        List<Map<String, Object>> entries = new ArrayList<>();
        for (Map<String, String> raw : etlTraceEntries) {
            Map<String, Object> entry = new LinkedHashMap<>(raw);

            boolean hasFineLine = raw.containsKey("java_line_start");

            String javaElem = raw.get("java_element");
            String javaType = raw.get("java_type");
            if (javaElem != null && javaPositions != null) {
                String simpleName = javaElem.contains(".")
                        ? javaElem.substring(javaElem.lastIndexOf('.') + 1)
                        : javaElem;
                enrichWithJavaSource(entry, simpleName, javaPositions, javaType);
            }

            if (hasFineLine) {
                entry.put("java_line_start", Integer.parseInt(raw.get("java_line_start")));
                entry.put("java_line_end", Integer.parseInt(raw.get("java_line_end")));
            }
            entries.add(entry);
        }

        Path outPath = outputDir.resolve("trace_m2m.json");
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
            System.out.println("M2M trace written to: " + outPath.toAbsolutePath()
                    + " (" + entries.size() + " entries)");
        } catch (IOException ex) {
            System.err.println("Warning: Failed to write M2M trace: " + ex.getMessage());
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

    private static String jsonValueOf(Object v) {
        if (v == null) return "null";
        if (v instanceof Number || v instanceof Boolean) return v.toString();
        return "\"" + escapeJson(String.valueOf(v)) + "\"";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static void printRoboChartSummary(Resource resource) {
        int states = 0, transitions = 0, events = 0;
        for (var it = resource.getAllContents(); it.hasNext(); ) {
            EObject obj = it.next();
            String cn = obj.eClass().getName();
            switch (cn) {
                case "State" -> states++;
                case "Transition" -> transitions++;
                case "Event" -> events++;
            }
        }
        System.out.printf("RoboChart: %d states, %d transitions, %d events%n",
                states, transitions, events);
    }
}
