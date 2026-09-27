package forge.transformations.t2m;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.path.CtRole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spoon.reflect.cu.SourcePosition;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import forge.transformations.m2t.RoboChart2RctTransformer;

/**
 * Discovers Java source files using Spoon and produces an EMF model conforming
 * to the auto-generated Spoon metamodel ({@code spoon.ecore}).
 *
 * <p>Uses Spoon's parser in no-classpath mode, then walks the Spoon AST and
 * creates corresponding EMF EObjects via a reflective mapper that uses
 * {@link CtRole}-based value access.
 */
public class SpoonDiscoverer {

    private static final Logger log = LoggerFactory.getLogger(SpoonDiscoverer.class);

    private Map<EObject, Object> resolvedValues = Map.of();
    private Map<String, List<RecordMetadataResolver.FieldInfo>> recordMetadata = Map.of();
    private Map<String, Object> namedConstants = Map.of();
    private SpoonToEmfMapper mapper;

    /**
     * Parse Java sources under the given root directory and return an EMF resource
     * conforming to the Spoon metamodel.
     *
     * <p>The resource contains one root EObject per top-level type found.
     * After calling this method, {@link #getResolvedValues()} returns a map of
     * EMF EObjects to their resolved literal/constant values from the Spoon AST.
     */
    public Resource discover(Path sourceRoot) throws IOException {
        SpoonJavaMetamodel mm = SpoonJavaMetamodel.getInstance();

        // Set up EMF resource
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("xmi", new XMIResourceFactoryImpl());
        rs.getPackageRegistry().put(SpoonJavaMetamodel.NS_URI, mm.ePackage());
        Resource resource = rs.createResource(URI.createURI("discovered_spoon_model.xmi"));

        // Parse with Spoon
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setComplianceLevel(17);
        launcher.getEnvironment().setShouldCompile(false);
        launcher.addInputResource(sourceRoot.toAbsolutePath().toString());
        launcher.buildModel();

        CtModel model = launcher.getModel();

        // Map all top-level types to EMF
        this.mapper = new SpoonToEmfMapper(mm);
        for (CtType<?> type : model.getAllTypes()) {
            EObject emfObj = mapper.map(type);
            if (emfObj != null) {
                resource.getContents().add(emfObj);
            }
        }

        // Resolve literal values and named constants from live Spoon AST
        this.resolvedValues = ValueResolver.resolve(model, mapper.getMapped());
        this.namedConstants = ValueResolver.buildConstantMap(model);

        // Extract record component metadata for per-event type declarations
        this.recordMetadata = RecordMetadataResolver.resolve(model);

        return resource;
    }

    /**
     * Returns the resolved literal/constant values from the most recent
     * {@link #discover(Path)} call. Keys are EMF EObjects from the returned
     * resource; values are the corresponding Java objects (Integer, Double, etc.).
     *
     * <p>Returns an empty map if {@link #discover(Path)} has not been called.
     */
    public Map<EObject, Object> getResolvedValues() {
        return resolvedValues;
    }

    /**
     * Returns the record component metadata from the most recent
     * {@link #discover(Path)} call. Keys are record simple names;
     * values are ordered lists of field info (name + Java type).
     *
     * <p>Returns an empty map if {@link #discover(Path)} has not been called.
     */
    public Map<String, List<RecordMetadataResolver.FieldInfo>> getRecordMetadata() {
        return recordMetadata;
    }

    /**
     * Returns named constants as camelCase name → value map, suitable for
     * {@link forge.transformations.m2t.RoboChart2RctTransformer#setConstantDefaults}.
     * Converts SCREAMING_SNAKE_CASE field names to camelCase.
     * Only includes unqualified keys (no "TypeName." prefix).
     */
    /**
     * Returns a map from EMF EObject (Spoon element) to its Java source
     * line range [lineStart, lineEnd] as a List for easy access from
     * Epsilon EOL (which cannot index Java arrays).
     */
    public Map<EObject, List<Integer>> getSourcePositionMap() {
        Map<EObject, List<Integer>> result = new java.util.IdentityHashMap<>();
        if (mapper == null) return result;
        for (Map.Entry<CtElement, EObject> e : mapper.getMapped().entrySet()) {
            SourcePosition pos = e.getKey().getPosition();
            if (pos != null && pos.isValidPosition()) {
                result.put(e.getValue(), List.of(pos.getLine(), pos.getEndLine()));
            }
        }
        return result;
    }

    public Map<String, Object> getConstantDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        for (Map.Entry<String, Object> entry : namedConstants.entrySet()) {
            String key = entry.getKey();
            // Skip qualified keys (e.g., "LreConstants.MIN_SAFE_DIST")
            if (key.contains(".")) continue;
            String camel = screamingSnakeToCamel(key);
            Object val = entry.getValue();
            // Convert to numeric string for EGL rendering
            if (val instanceof Double d) {
                defaults.put(camel, d.intValue() == d ? String.valueOf(d.intValue()) : String.valueOf(d));
            } else if (val instanceof Integer i) {
                defaults.put(camel, String.valueOf(i));
            } else {
                defaults.put(camel, String.valueOf(val));
            }
        }
        return defaults;
    }

    private static String screamingSnakeToCamel(String name) {
        String[] parts = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            if (i == 0) {
                sb.append(parts[i].toLowerCase());
            } else {
                sb.append(parts[i].substring(0, 1).toUpperCase());
                sb.append(parts[i].substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * Write a traceability file ({@code trace_t2m.json}) that maps each discovered
     * Java element to its source file and line range. This enables downstream
     * stages to trace RoboChart/CSP-M elements back to Java source locations.
     */
    public void writeTraceFile(Path outputDir) {
        if (mapper == null) {
            log.warn("writeTraceFile called before discover() — skipping");
            return;
        }

        List<Map<String, Object>> entries = new ArrayList<>();
        for (Map.Entry<CtElement, EObject> e : mapper.getMapped().entrySet()) {
            CtElement ctElem = e.getKey();
            EObject emfObj = e.getValue();

            SourcePosition pos = ctElem.getPosition();
            // isValidPosition() returning true does not guarantee getFile()
            // is non-null — in-memory or generated CompilationUnits (e.g.
            // implicit synthesis under setNoClasspath) can have positions
            // without backing files. Skip those.
            if (pos == null || !pos.isValidPosition() || pos.getFile() == null) continue;

            // Build qualified name: for types use getQualifiedName, else parent.simpleName.elementName
            String qualName = qualifiedName(ctElem);
            if (qualName == null) continue;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("emf_type", emfObj.eClass().getName());
            entry.put("qualified_name", qualName);
            entry.put("file", pos.getFile().getPath());
            entry.put("line_start", pos.getLine());
            entry.put("line_end", pos.getEndLine());
            entries.add(entry);
        }

        // Sort by file then line_start for readability
        entries.sort(Comparator
                .<Map<String, Object>, String>comparing(m -> (String) m.get("file"))
                .thenComparingInt(m -> (int) m.get("line_start")));

        // Write JSON manually (no external library needed)
        Path outPath = outputDir.resolve("trace_t2m.json");
        try (Writer w = Files.newBufferedWriter(outPath)) {
            w.write("{\n  \"source_positions\": [\n");
            for (int i = 0; i < entries.size(); i++) {
                Map<String, Object> entry = entries.get(i);
                w.write("    {");
                w.write("\"emf_type\": " + jsonStr(entry.get("emf_type")));
                w.write(", \"qualified_name\": " + jsonStr(entry.get("qualified_name")));
                w.write(", \"file\": " + jsonStr(entry.get("file")));
                w.write(", \"line_start\": " + entry.get("line_start"));
                w.write(", \"line_end\": " + entry.get("line_end"));
                w.write("}");
                if (i < entries.size() - 1) w.write(",");
                w.write("\n");
            }
            w.write("  ]\n}\n");
            log.info("Trace written to {} ({} entries)", outPath, entries.size());
        } catch (IOException ex) {
            log.error("Failed to write trace file: {}", ex.getMessage());
        }
    }

    private static String qualifiedName(CtElement elem) {
        if (elem instanceof CtType<?> type) {
            return type.getQualifiedName();
        }
        // For methods, fields, constructors: parentType.simpleName.memberName
        if (elem instanceof spoon.reflect.declaration.CtNamedElement named) {
            CtType<?> parent = elem.getParent(CtType.class);
            if (parent != null) {
                return parent.getQualifiedName() + "." + named.getSimpleName();
            }
            return named.getSimpleName();
        }
        return null;
    }

    private static String jsonStr(Object val) {
        if (val == null) return "null";
        String s = val.toString()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
        return "\"" + s + "\"";
    }

    /**
     * Returns a map from simple name → list of source position entries.
     * Each entry has: qualified_name, emf_type, file, line_start, line_end.
     * Used by the M2M trace writer to cross-reference RoboChart elements
     * back to their Java source.
     */
    public Map<String, List<Map<String, Object>>> getSourcePositionsByName() {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        if (mapper == null) return result;

        for (Map.Entry<CtElement, EObject> e : mapper.getMapped().entrySet()) {
            CtElement ctElem = e.getKey();
            EObject emfObj = e.getValue();

            SourcePosition pos = ctElem.getPosition();
            // See note in writeTraceFile: getFile() can be null on in-memory
            // CompilationUnits even when isValidPosition() is true.
            if (pos == null || !pos.isValidPosition() || pos.getFile() == null) continue;

            String qualName = qualifiedName(ctElem);
            if (qualName == null) continue;

            // Simple name is the last segment
            String simpleName = qualName.contains(".")
                    ? qualName.substring(qualName.lastIndexOf('.') + 1)
                    : qualName;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("qualified_name", qualName);
            entry.put("emf_type", emfObj.eClass().getName());
            entry.put("file", pos.getFile().getPath());
            entry.put("line_start", pos.getLine());
            entry.put("line_end", pos.getEndLine());

            result.computeIfAbsent(simpleName, k -> new ArrayList<>()).add(entry);
        }
        return result;
    }

    /**
     * Reflective mapper from Spoon CtElements to EMF EObjects conforming to spoon.ecore.
     *
     * <p>Uses {@link CtRole} to get values from Spoon elements, which is reliable
     * regardless of getter naming conventions. Uses an IdentityHashMap to track
     * already-mapped elements and prevent infinite recursion on cycles.
     */
    static class SpoonToEmfMapper {

        private static final Logger log = LoggerFactory.getLogger(SpoonToEmfMapper.class);

        private final SpoonJavaMetamodel mm;
        private final IdentityHashMap<CtElement, EObject> mapped = new IdentityHashMap<>();

        SpoonToEmfMapper(SpoonJavaMetamodel mm) {
            this.mm = mm;
        }

        Map<CtElement, EObject> getMapped() {
            return Collections.unmodifiableMap(mapped);
        }

        EObject map(CtElement element) {
            if (element == null) return null;
            if (mapped.containsKey(element)) return mapped.get(element);

            String className = spoonClassName(element);
            EClass eClass = findEClass(className);
            if (eClass == null) return null;

            EObject obj = EcoreUtil.create(eClass);
            mapped.put(element, obj);

            // Set all structural features that exist on this EClass
            for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
                try {
                    setFeature(obj, feature, element);
                } catch (Exception e) {
                    log.debug("Failed to map feature {}.{}: {}",
                            eClass.getName(), feature.getName(), e.getMessage());
                }
            }

            return obj;
        }

        private void setFeature(EObject obj, EStructuralFeature feature, CtElement element) {
            String featureName = feature.getName();

            // Convert feature name to CtRole and get value
            Object value = getValueByFeatureName(element, featureName);
            if (value == null) return;

            if (feature instanceof EAttribute attr) {
                setAttributeValue(obj, attr, value);
            } else if (feature instanceof EReference ref) {
                setReferenceValue(obj, ref, value);
            }
        }

        /**
         * Get a value from a Spoon element using CtRole.
         * Converts camelCase feature name to UPPER_SNAKE_CASE CtRole name.
         */
        private Object getValueByFeatureName(CtElement element, String featureName) {
            String roleName = camelToUpperSnake(featureName);
            try {
                CtRole role = CtRole.valueOf(roleName);
                return element.getValueByRole(role);
            } catch (IllegalArgumentException e) {
                // No matching CtRole for this feature name
                return null;
            }
        }

        /**
         * Convert camelCase to UPPER_SNAKE_CASE.
         * e.g. "leftOperand" → "LEFT_OPERAND", "operatorKind" → "OPERATOR_KIND"
         */
        private String camelToUpperSnake(String camelCase) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < camelCase.length(); i++) {
                char c = camelCase.charAt(i);
                if (Character.isUpperCase(c) && i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toUpperCase(c));
            }
            return sb.toString();
        }

        @SuppressWarnings("unchecked")
        private void setAttributeValue(EObject obj, EAttribute attr, Object value) {
            EClassifier type = attr.getEAttributeType();
            if (type instanceof EEnum eEnum) {
                // Map Spoon enum to EMF enum literal.
                // Handle collections (e.g. modifier: Set<ModifierKind>{PRIVATE, FINAL}).
                if (attr.isMany() && value instanceof Collection<?> coll) {
                    EList<Object> list = (EList<Object>) obj.eGet(attr);
                    for (Object item : coll) {
                        String litName = item instanceof Enum<?> e ? e.name() : item.toString();
                        EEnumLiteral lit = eEnum.getEEnumLiteral(litName);
                        if (lit != null) list.add(lit.getInstance());
                    }
                } else {
                    String literalName = value instanceof Enum<?> e ? e.name() : value.toString();
                    EEnumLiteral lit = eEnum.getEEnumLiteral(literalName);
                    if (lit != null) {
                        if (attr.isMany()) {
                            ((EList<Object>) obj.eGet(attr)).add(lit.getInstance());
                        } else {
                            obj.eSet(attr, lit.getInstance());
                        }
                    }
                }
            } else {
                // Primitive types: String, boolean, int, etc.
                if (attr.isMany()) {
                    if (value instanceof Collection<?> coll) {
                        EList<Object> list = (EList<Object>) obj.eGet(attr);
                        for (Object item : coll) {
                            Object converted = convertPrimitive(item, type);
                            if (converted != null) list.add(converted);
                        }
                    }
                } else {
                    Object converted = convertPrimitive(value, type);
                    if (converted != null) {
                        obj.eSet(attr, converted);
                    }
                }
            }
        }

        private Object convertPrimitive(Object value, EClassifier targetType) {
            if (value == null) return null;
            String typeName = targetType.getName();
            return switch (typeName) {
                case "EString" -> value.toString();
                case "EBoolean" -> {
                    if (value instanceof Boolean b) yield b;
                    yield Boolean.parseBoolean(value.toString());
                }
                case "EInt" -> {
                    if (value instanceof Number n) yield n.intValue();
                    yield null;
                }
                case "ELong" -> {
                    if (value instanceof Number n) yield n.longValue();
                    yield null;
                }
                default -> null;
            };
        }

        @SuppressWarnings("unchecked")
        private void setReferenceValue(EObject obj, EReference ref, Object value) {
            if (ref.isMany()) {
                if (value instanceof Collection<?> coll) {
                    EList<EObject> list = (EList<EObject>) obj.eGet(ref);
                    for (Object item : coll) {
                        if (item instanceof CtElement child) {
                            EObject childObj = map(child);
                            if (childObj != null) {
                                list.add(childObj);
                            }
                        }
                    }
                }
            } else {
                if (value instanceof CtElement child) {
                    EObject childObj = map(child);
                    if (childObj != null) {
                        obj.eSet(ref, childObj);
                    }
                }
            }
        }

        private String spoonClassName(CtElement element) {
            // Spoon implementation classes follow CtXxxImpl → interface CtXxx
            // Find the most specific Spoon interface
            for (Class<?> iface : element.getClass().getInterfaces()) {
                String name = iface.getSimpleName();
                if (name.startsWith("Ct") && !name.endsWith("Impl")) {
                    return name;
                }
            }
            // Fallback: strip "Impl" suffix
            String name = element.getClass().getSimpleName();
            if (name.endsWith("Impl")) {
                return name.substring(0, name.length() - 4);
            }
            return name;
        }

        private EClass findEClass(String name) {
            try {
                return mm.eClass(name);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
