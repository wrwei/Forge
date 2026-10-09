package forge.transformations.m2t;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import forge.transformations.t2m.SpoonJavaMetamodel;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forge.transformations.core.EglGenerationRunner;
import forge.transformations.t2m.SpoonDiscoverer;

/**
 * Generates Dafny verification code (.dfy) from a Spoon Java EMF model
 * using an EGL template ({@code transformations/java2dafny.egl}).
 *
 * <p>The EGL template walks the Spoon EMF model to extract the mode enum,
 * controller class, state fields, and step() method if-else chain, then
 * generates Dafny source with mode datatype, abstracted functions,
 * transition methods with requires/ensures contracts, and determinism lemmas.
 *
 * <p>Note: this is the only Dafny generator. An earlier Java-string-building
 * variant ({@code Java2DafnyTransformer}) was retired in favour of this
 * EGL-based implementation; references to the old class name in older docs
 * or commit messages refer to logic now consolidated here.
 */
public class Java2DafnyEglTransformer {

    private static final String EGL_RESOURCE = "transformations/java2dafny.egl";

    private Map<EObject, Object> resolvedValues = Map.of();

    /** Trace entries collected during the most recent EGL execution. */
    private List<Map<String, String>> traceEntries = List.of();

    public void setResolvedValues(Map<EObject, Object> resolvedValues) {
        this.resolvedValues = resolvedValues != null ? resolvedValues : Map.of();
    }

    /**
     * Returns the trace entries from the most recent transformation.
     * Each entry maps Dafny constructs (methods, lemmas, functions) to their
     * output line ranges (dafny_line_start, dafny_line_end, dafny_type, dafny_element).
     */
    public List<Map<String, String>> getTraceEntries() {
        return traceEntries;
    }

    /**
     * Transform the Spoon EMF model into Dafny verification code.
     *
     * @param spoonResource the Spoon EMF resource (from SpoonDiscoverer)
     * @param outputFile    path to write the generated .dfy file
     * @return the generated Dafny text
     */
    public String transform(Resource spoonResource, Path outputFile) {
        try {
            URL eglUrl = getClass().getClassLoader().getResource(EGL_RESOURCE);
            if (eglUrl == null) {
                throw new IllegalStateException("Cannot find " + EGL_RESOURCE + " on classpath");
            }
            Path eglPath = Path.of(eglUrl.toURI());

            Map<String, Object> variables = new HashMap<>();
            variables.put("resolvedValues", resolvedValues);

            EglGenerationRunner runner = new EglGenerationRunner();
            String result = runner.run(
                    eglPath,
                    "Spoon", spoonResource, SpoonJavaMetamodel.getInstance().ePackage(),
                    outputFile,
                    variables
            );

            // Extract trace entries by parsing the generated Dafny output
            this.traceEntries = extractDafnyTrace(result);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Dafny EGL generation from Spoon model failed", e);
        }
    }

    // Patterns for Dafny constructs
    private static final Pattern DATATYPE_MODE = Pattern.compile("^datatype Mode\\b");
    private static final Pattern DATATYPE_EVENT = Pattern.compile("^datatype InputEvent\\b");
    private static final Pattern FUNCTION_DECL = Pattern.compile("^function (\\w+)\\(");
    private static final Pattern CLASS_DECL = Pattern.compile("^class (\\w+)\\s*\\{");
    private static final Pattern METHOD_TRANSITION = Pattern.compile("^method transitionFrom(\\w+)\\(");
    private static final Pattern METHOD_STEP = Pattern.compile("^method step\\(");
    private static final Pattern CONSTRUCTOR = Pattern.compile("^constructor\\(");
    private static final Pattern GHOST_PRED = Pattern.compile("^ghost predicate (\\w+)\\(");
    private static final Pattern LEMMA_DECL = Pattern.compile("^lemma (\\w+)\\(");

    /**
     * Parse the generated Dafny text to extract trace entries mapping
     * Dafny constructs to their output line ranges.
     */
    private static List<Map<String, String>> extractDafnyTrace(String dafnyText) {
        List<Map<String, String>> entries = new ArrayList<>();
        if (dafnyText == null || dafnyText.isEmpty()) return entries;

        String[] lines = dafnyText.split("\n");

        // Stack for tracking nested brace blocks
        record Block(String type, String name, int startLine) {}
        Deque<Block> blockStack = new ArrayDeque<>();
        // Pending block: header seen, waiting for opening "{"
        String pendingType = null;
        String pendingName = null;
        int pendingStart = 0;

        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            int lineNum = i + 1;

            // Single-line datatypes
            Matcher m;
            if ((m = DATATYPE_MODE.matcher(trimmed)).find()) {
                entries.add(entry("ModeDatatype", "Mode", lineNum, lineNum));
            }
            if ((m = DATATYPE_EVENT.matcher(trimmed)).find()) {
                entries.add(entry("EventDatatype", "InputEvent", lineNum, lineNum));
            }

            // Abstract function declarations (2 lines: decl + "reads this")
            if ((m = FUNCTION_DECL.matcher(trimmed)).find()) {
                entries.add(entry("AbstractFunction", m.group(1), lineNum, lineNum + 1));
            }

            // Block headers — set pending, activated when "{" is seen
            if ((m = CLASS_DECL.matcher(trimmed)).find()) {
                // class header has "{" on same line
                blockStack.push(new Block("Controller", m.group(1), lineNum));
            } else if ((m = METHOD_TRANSITION.matcher(trimmed)).find()) {
                pendingType = "TransitionMethod";
                pendingName = m.group(1);
                pendingStart = lineNum;
            } else if (METHOD_STEP.matcher(trimmed).find()) {
                pendingType = "StepMethod";
                pendingName = "step";
                pendingStart = lineNum;
            } else if (CONSTRUCTOR.matcher(trimmed).find()) {
                pendingType = "Constructor";
                pendingName = "constructor";
                pendingStart = lineNum;
            } else if ((m = GHOST_PRED.matcher(trimmed)).find()) {
                pendingType = "GhostPredicate";
                pendingName = m.group(1);
                pendingStart = lineNum;
            } else if ((m = LEMMA_DECL.matcher(trimmed)).find()) {
                pendingType = "Lemma";
                pendingName = m.group(1);
                pendingStart = lineNum;
            }

            // Opening brace on its own line activates a pending block
            if ("{".equals(trimmed) && pendingType != null) {
                blockStack.push(new Block(pendingType, pendingName, pendingStart));
                pendingType = null;
                pendingName = null;
            }

            // Closing brace pops the innermost block
            if ("}".equals(trimmed) && !blockStack.isEmpty()) {
                Block block = blockStack.pop();
                entries.add(entry(block.type, block.name, block.startLine, lineNum));
            }
        }
        return entries;
    }

    private static Map<String, String> entry(String type, String element, int startLine, int endLine) {
        Map<String, String> e = new LinkedHashMap<>();
        e.put("dafny_type", type);
        e.put("dafny_element", element);
        e.put("dafny_line_start", String.valueOf(startLine));
        e.put("dafny_line_end", String.valueOf(endLine));
        return e;
    }
}
