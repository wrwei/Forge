package forge.transformations.m2t;

import org.eclipse.emf.ecore.resource.Resource;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.transformations.core.EglGenerationRunner;
import forge.transformations.m2m.Java2RoboChartTransformer;
import forge.transformations.m2m.RoboChartMetamodel;

/**
 * Generates RoboChart textual notation (.rct) from a RoboChart state machine EMF model.
 *
 * <p>This transformer delegates to an Epsilon EGL template
 * ({@code transformations/robochart2rct.egl}) which walks the RoboChart model
 * and produces a .rct file compatible with the RoboChart tool's textual syntax.
 */
public class RoboChart2RctTransformer {

    private static final String EGL_RESOURCE = "transformations/robochart2rct.egl";

    /** Trace entries collected during the most recent EGL execution. */
    private List<Map<String, String>> traceEntries = List.of();

    /** Optional constant default values: name → value (e.g., "minSafeDist" → 1). */
    private Map<String, Object> constantDefaults = new HashMap<>();

    /** Specification-level elements (datatypes, functions, sensors, actuators). */
    private Map<String, Object> specElements = new HashMap<>();

    /** Set default values for constants (rendered as {@code = value} in the .rct). */
    public void setConstantDefaults(Map<String, Object> defaults) {
        this.constantDefaults = defaults != null ? defaults : new HashMap<>();
    }

    /** Set specification-level elements for rendering (datatypes, functions, interfaces). */
    public void setSpecElements(Map<String, Object> elements) {
        this.specElements = elements != null ? elements : new HashMap<>();
    }

    /**
     * Returns the M2T trace entries from the most recent transformation.
     * Each entry maps RoboChart elements to their RCT line ranges
     * (e.g., State → lines 3-5, Transition → lines 12-16).
     */
    public List<Map<String, String>> getTraceEntries() {
        return traceEntries;
    }

    /**
     * Transform a RoboChart EMF resource into RoboChart textual notation.
     *
     * @param roboChartResource the RoboChart EMF resource (from Java2RoboChartTransformer)
     * @param outputFile        path to write the generated .rct file
     * @return the generated RCT text
     */
    public String transform(Resource roboChartResource, Path outputFile) {
        try {
            // Validate that the model contains a state machine before running EGL
            if (roboChartResource.getContents().isEmpty()) {
                throw new IllegalStateException(
                    "RoboChart model is empty — no elements found.\n"
                    + "The ETL transformation produced 0 states and 0 transitions.\n"
                    + "This usually means the Java source has no controller class with a step() method.\n"
                    + "Ensure your Java code includes a controller before running RCT generation.");
            }
            var rootObj = roboChartResource.getContents().get(0);
            var machines = rootObj.eClass().getEStructuralFeature("machines");
            if (machines != null) {
                var machineList = (java.util.Collection<?>) rootObj.eGet(
                        rootObj.eClass().getEStructuralFeature("machines"));
                if (machineList == null || machineList.isEmpty()) {
                    throw new IllegalStateException(
                        "RoboChart model has no state machines.\n"
                        + "The ETL transformation found types but no controller state machine.\n"
                        + "Ensure your Java code includes a controller class with:\n"
                        + "  - A mode enum field (e.g., LreMode currentMode)\n"
                        + "  - A public void step(InputEvent event) method\n"
                        + "  - A mode-nested if-else structure inside step()");
                }
            }

            URL eglUrl = getClass().getClassLoader().getResource(EGL_RESOURCE);
            if (eglUrl == null) {
                throw new IllegalStateException("Cannot find " + EGL_RESOURCE + " on classpath");
            }
            Path eglPath = Path.of(eglUrl.toURI());

            // Mutable list injected into EGL — post-processing appends trace entries
            ArrayList<Map<String, String>> traceList = new ArrayList<>();
            Map<String, Object> variables = new HashMap<>();
            variables.put("traceEntries", traceList);
            variables.put("constantDefaults", constantDefaults);

            EglGenerationRunner runner = new EglGenerationRunner();
            String rct = runner.run(
                    eglPath,
                    "RoboChart", roboChartResource, RoboChartMetamodel.getInstance().ePackage(),
                    outputFile,
                    variables
            );

            // Capture trace entries collected during EGL execution
            this.traceEntries = traceList;

            return rct;
        } catch (IllegalStateException e) {
            throw new RuntimeException("RCT generation failed: " + e.getMessage());
        } catch (Exception e) {
            String causeMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            throw new RuntimeException("EGL generation failed: " + causeMsg
                + "\nCheck that the RoboChart model (.xmi) contains a valid state machine"
                + " with states, transitions, and events.", e);
        }
    }
}
