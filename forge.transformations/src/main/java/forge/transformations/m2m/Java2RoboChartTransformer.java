package forge.transformations.m2m;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import forge.transformations.t2m.SpoonJavaMetamodel;

import forge.transformations.t2m.RecordMetadataResolver;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.transformations.core.EtlTransformRunner;
import forge.transformations.t2m.SpoonDiscoverer;

/**
 * Extracts a RoboChart state machine model from a MoDisco Java EMF model.
 *
 * <p>This transformer delegates to an Epsilon ETL script
 * ({@code transformations/java2robochart.etl}) which pattern-matches on a flat
 * if-else state machine structure found in the Java source code. It expects:
 * <ul>
 *   <li>An enum declaration defining the operating modes (states)</li>
 *   <li>A class with a single {@code step()} method containing a flat if-else chain</li>
 *   <li>Each if-branch: condition referencing current mode + optional event type check,
 *       body containing mode assignment + optional actions</li>
 * </ul>
 *
 * <p>The extraction walks the MoDisco IfStatement chain and, for each branch,
 * identifies the source state, target state, trigger event, guard condition,
 * and action statements. These are mapped to RoboChart {@code State},
 * {@code Transition}, {@code Event}, and action {@code Statement} EObjects.
 */
public class Java2RoboChartTransformer {

    private static final String ETL_RESOURCE = "transformations/java2robochart.etl";

    private static final List<String> DEFAULT_ENUM_SUFFIXES = List.of("Mode", "State");
    private static final String DEFAULT_STEP_METHOD_NAME = "step";
    private static final String DEFAULT_MODE_FIELD_NAME = "currentMode";

    /** Trace entries collected during the most recent ETL execution. */
    private List<Map<String, String>> traceEntries = List.of();

    /** Optional EObject → [lineStart, lineEnd] map for fine-grained source positions. */
    private Map<EObject, List<Integer>> sourcePositionMap;

    /**
     * Set the source position map so the ETL can record fine-grained
     * Java line numbers per Spoon element (e.g., the CtIf that produced
     * a specific transition).
     */
    public void setSourcePositionMap(Map<EObject, List<Integer>> sourcePositionMap) {
        this.sourcePositionMap = sourcePositionMap;
    }

    /**
     * Returns the M2M trace entries from the most recent transformation.
     * Each entry maps RoboChart elements back to the Java elements they
     * were derived from (e.g., enum literal → State, event type → Event).
     */
    public List<Map<String, String>> getTraceEntries() {
        return traceEntries;
    }

    /**
     * Transform a Java resource into a RoboChart state machine resource
     * using the default naming conventions ({@code *Mode}/{@code *State} enum,
     * {@code step()} method, {@code currentMode} field).
     *
     * @param javaResource the Java EMF resource (from SpoonDiscoverer)
     * @return a new EMF resource containing the RoboChart state machine model
     */
    public Resource transform(Resource javaResource) {
        return transform(javaResource, DEFAULT_ENUM_SUFFIXES,
                         DEFAULT_STEP_METHOD_NAME, DEFAULT_MODE_FIELD_NAME, null, null);
    }

    /**
     * Transform a Java resource into a RoboChart state machine resource
     * with resolved literal/constant values from the Spoon AST.
     *
     * @param javaResource   the Java EMF resource (from SpoonDiscoverer)
     * @param resolvedValues map of EMF EObjects to resolved values (from
     *                       {@link forge.transformations.t2m.SpoonDiscoverer#getResolvedValues()})
     * @return a new EMF resource containing the RoboChart state machine model
     */
    public Resource transform(Resource javaResource, Map<EObject, Object> resolvedValues) {
        return transform(javaResource, DEFAULT_ENUM_SUFFIXES,
                         DEFAULT_STEP_METHOD_NAME, DEFAULT_MODE_FIELD_NAME, resolvedValues, null);
    }

    /**
     * Transform a Java resource into a RoboChart state machine resource
     * with resolved values and record component metadata for per-event typing.
     *
     * @param javaResource   the Java EMF resource (from SpoonDiscoverer)
     * @param resolvedValues map of EMF EObjects to resolved values
     * @param recordMetadata map of record simple names to their field info
     * @return a new EMF resource containing the RoboChart state machine model
     */
    public Resource transform(Resource javaResource,
                              Map<EObject, Object> resolvedValues,
                              Map<String, List<RecordMetadataResolver.FieldInfo>> recordMetadata) {
        return transform(javaResource, DEFAULT_ENUM_SUFFIXES,
                         DEFAULT_STEP_METHOD_NAME, DEFAULT_MODE_FIELD_NAME,
                         resolvedValues, recordMetadata);
    }

    /**
     * Transform a Java resource into a RoboChart state machine resource
     * with configurable naming conventions.
     *
     * @param javaResource   the Java EMF resource (from SpoonDiscoverer)
     * @param enumSuffixes   suffixes to match the mode/state enum (e.g. {@code ["Mode", "State"]})
     * @param stepMethodName name of the step method in the controller class
     * @param modeFieldName  name of the current-mode field in the controller class
     * @return a new EMF resource containing the RoboChart state machine model
     */
    public Resource transform(Resource javaResource,
                              List<String> enumSuffixes,
                              String stepMethodName,
                              String modeFieldName) {
        return transform(javaResource, enumSuffixes, stepMethodName, modeFieldName, null, null);
    }

    /**
     * Transform a Java resource into a RoboChart state machine resource
     * with configurable naming conventions, resolved values, and record metadata.
     *
     * @param javaResource   the Java EMF resource (from SpoonDiscoverer)
     * @param enumSuffixes   suffixes to match the mode/state enum (e.g. {@code ["Mode", "State"]})
     * @param stepMethodName name of the step method in the controller class
     * @param modeFieldName  name of the current-mode field in the controller class
     * @param resolvedValues map of EMF EObjects to resolved values, or {@code null}
     * @param recordMetadata map of record simple names to field info, or {@code null}
     * @return a new EMF resource containing the RoboChart state machine model
     */
    public Resource transform(Resource javaResource,
                              List<String> enumSuffixes,
                              String stepMethodName,
                              String modeFieldName,
                              Map<EObject, Object> resolvedValues,
                              Map<String, List<RecordMetadataResolver.FieldInfo>> recordMetadata) {
        try {
            URL etlUrl = getClass().getClassLoader().getResource(ETL_RESOURCE);
            if (etlUrl == null) {
                throw new IllegalStateException("Cannot find " + ETL_RESOURCE + " on classpath");
            }
            Path etlPath = Path.of(etlUrl.toURI());

            // Mutable list injected into ETL — operations append trace entries during transformation
            ArrayList<Map<String, String>> traceList = new ArrayList<>();

            Map<String, Object> variables = new HashMap<>();
            variables.put("enumSuffixes", enumSuffixes != null ? enumSuffixes : DEFAULT_ENUM_SUFFIXES);
            variables.put("stepMethodName", stepMethodName != null ? stepMethodName : DEFAULT_STEP_METHOD_NAME);
            variables.put("modeFieldName", modeFieldName != null ? modeFieldName : DEFAULT_MODE_FIELD_NAME);
            variables.put("traceEntries", traceList);
            if (resolvedValues != null && !resolvedValues.isEmpty()) {
                variables.put("resolvedValues", resolvedValues);
            }
            if (sourcePositionMap != null && !sourcePositionMap.isEmpty()) {
                variables.put("sourcePositionMap", sourcePositionMap);
            }
            // Always set recordMetadata so ETL can reference it (even if empty).
            // Use HashMap (not Collections.emptyMap()) — Epsilon's EOL uses reflection
            // and cannot access the internal Collections$EmptyMap class under Java 17+ modules.
            variables.put("recordMetadata",
                    recordMetadata != null ? new HashMap<>(recordMetadata) : new HashMap<>());

            EtlTransformRunner runner = new EtlTransformRunner();
            Resource result = runner.run(
                    etlPath,
                    "Spoon", javaResource, SpoonJavaMetamodel.getInstance().ePackage(),
                    "RoboChart", RoboChartMetamodel.getInstance().ePackage(),
                    variables
            );

            // Capture trace entries collected during ETL execution
            this.traceEntries = traceList;

            return result;
        } catch (Exception e) {
            throw new RuntimeException("ETL transformation failed", e);
        }
    }
}
