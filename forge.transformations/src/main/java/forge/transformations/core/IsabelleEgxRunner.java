package forge.transformations.core;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Orchestrates Isabelle/UTP Z-Machine theory generation via the
 * forked EGL template at
 * {@code transformations/thy_generation_rule.egl} (loaded from the
 * runtime classpath, same pattern as the Dafny / RCT runners).
 *
 * <p>The template is derived from {@code thy_generation_rule.egl} in the
 * ICECCS2023 archive (vendored at {@code docs/archive/ICECCS2023/}),
 * with local patches applied for bugs the archive authors did not hit.
 * See {@code docs/fixes/I2_template_fork.md} for the diff against
 * upstream and the rationale for each patch.
 *
 * <p>Outputs:
 * <ul>
 *   <li>{@code <output>/isabelle/<StmName>_Beh.thy} — generated theory</li>
 *   <li>{@code <output>/isabelle/ROOT} — session file declaring
 *       {@code session <StmName>_Check = "Z_Machines" + theories
 *       <StmName>_Beh} so that {@code isabelle build} can verify it</li>
 * </ul>
 */
public class IsabelleEgxRunner {

    private static final String EGL_RESOURCE = "transformations/thy_generation_rule.egl";

    /**
     * Generate the Isabelle theory and its ROOT session file.
     *
     * @param rcResource   the RoboChart EMF model (from ETL transform output)
     * @param rcMetamodel  the RoboChart EPackage
     * @param outputDir    directory to place generated files
     * @return the path of the generated {@code .thy} file
     */
    public Path run(Resource rcResource, EPackage rcMetamodel,
                    Path outputDir) throws Exception {
        return runAll(rcResource, rcMetamodel, outputDir).get(0);
    }

    /**
     * MULTI-CONTROLLER (2026-09-25). One theory per state machine, in the
     * package's machine order, all listed in a single ROOT session so that
     * {@code isabelle build -D} checks every one. A model with one machine
     * produces exactly the files it did before.
     */
    public java.util.List<Path> runAll(Resource rcResource, EPackage rcMetamodel,
                                       Path outputDir) throws Exception {
        java.util.List<String> stmNames = findStateMachineNames(rcResource);
        if (stmNames.isEmpty()) {
            throw new IllegalStateException(
                "No StateMachineDef found in RoboChart model; cannot derive stm_name");
        }

        URL eglUrl = getClass().getClassLoader().getResource(EGL_RESOURCE);
        if (eglUrl == null) {
            throw new IllegalStateException("Cannot find " + EGL_RESOURCE + " on classpath");
        }
        Path eglPath = Path.of(eglUrl.toURI());

        Path isabelleDir = outputDir.resolve("isabelle");
        Files.createDirectories(isabelleDir);
        java.util.List<String> lenses = classifyStatefulLenses(rcResource);

        java.util.List<Path> out = new java.util.ArrayList<>();
        for (int idx = 0; idx < stmNames.size(); idx++) {
            String stmName = stmNames.get(idx);
            Path thyPath = isabelleDir.resolve(stmName + "_Beh.thy");
            Map<String, Object> vars = new HashMap<>();
            vars.put("stm_name", stmName);
            vars.put("stm_index", idx);
            vars.put("statefulLensNames", lenses);
            EglGenerationRunner runner = new EglGenerationRunner();
            runner.run(eglPath, "RC", rcResource, rcMetamodel, thyPath, vars);
            System.out.println("Isabelle theory written to: " + thyPath.toAbsolutePath());
            out.add(thyPath);
        }
        writeRootFile(isabelleDir, stmNames);
        return out;
    }

    /**
     * FORK (I1 guard-skew, sranger cycle counter): classify the data-plane
     * definitions (operation-body assignments) as MEMORYLESS or STATEFUL and
     * return the stateful ones, in definition order.
     *
     * <p>A definition is STATEFUL when it transitively reads MUTABLE state: a
     * Ctrl_State variable with no per-cycle definition of its own (its
     * pre-state is genuine memory, e.g. sranger's {@code turnStartCycle},
     * written only by a transition action), or a self-reading definition
     * (sranger's {@code cycleCount = cycleCount + 1}). For such definitions
     * the value the Java dispatches on differs from the pre-state lens the
     * emitted precondition reads, so the EGL template must inline the
     * definition into the guard. MEMORYLESS definitions (all of LRE's and the
     * chemical detector's) are grounded entirely in uninterpreted sensor
     * calls and constants, where the bare pre-state lens read is faithful.
     *
     * <p>Computed HERE rather than in the template because template-side
     * traversal of the definition expressions measurably perturbs the
     * iteration order of hash-ordered collections that downstream emission
     * loops iterate (verified by byte-diff controls on the LRE theory). This
     * walk touches the model only via {@code eGet}/{@code eAllContents} and
     * hashes only strings, so it leaves the EGL run's emission order intact —
     * regeneration of the memoryless studies is byte-identical.
     *
     * <p>Mirrors the template's data-plane collection: every OperationDef
     * transition action contributes {@code Assignment} statements keyed by
     * LHS variable name (state entry actions are deliberately NOT part of the
     * per-cycle data plane, matching {@code dataDefOrder}).
     */
    static java.util.List<String> classifyStatefulLenses(Resource rcResource) {
        java.util.LinkedHashSet<String> stateVars = new java.util.LinkedHashSet<>();
        java.util.LinkedHashMap<String, EObject> defs = new java.util.LinkedHashMap<>();

        for (var it = rcResource.getAllContents(); it.hasNext(); ) {
            EObject o = it.next();
            String cls = o.eClass().getName();
            if ("Interface".equals(cls) && "Ctrl_State".equals(stringFeature(o, "name"))) {
                for (Object vlO : listFeature(o, "variableList")) {
                    for (Object vO : listFeature((EObject) vlO, "vars")) {
                        String vn = stringFeature((EObject) vO, "name");
                        if (vn != null) stateVars.add(vn);
                    }
                }
            }
            if ("OperationDef".equals(cls)) {
                for (Object trO : listFeature(o, "transitions")) {
                    EObject action = refFeature((EObject) trO, "action");
                    if (action == null) continue;
                    if ("SeqStatement".equals(action.eClass().getName())) {
                        for (Object stO : listFeature(action, "statements")) {
                            collectAssignment((EObject) stO, defs);
                        }
                    } else {
                        collectAssignment(action, defs);
                    }
                }
            }
        }

        // Mutable state: Ctrl_State vars with no per-cycle definition, plus
        // self-reading definitions (the counter shape).
        java.util.LinkedHashSet<String> mutable = new java.util.LinkedHashSet<>();
        for (String sv : stateVars) {
            if (!defs.containsKey(sv)) mutable.add(sv);
        }
        java.util.Map<String, java.util.Set<String>> reads = new HashMap<>();
        for (var e : defs.entrySet()) {
            java.util.Set<String> r = new java.util.LinkedHashSet<>();
            collectReadNames(e.getValue(), r);
            reads.put(e.getKey(), r);
            if (r.contains(e.getKey())) mutable.add(e.getKey());
        }

        // Least fixpoint: a definition reading a mutable or stateful name is stateful.
        java.util.LinkedHashSet<String> stateful = new java.util.LinkedHashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (var e : defs.entrySet()) {
                if (stateful.contains(e.getKey())) continue;
                for (String r : reads.get(e.getKey())) {
                    if (mutable.contains(r) || stateful.contains(r)) {
                        stateful.add(e.getKey());
                        changed = true;
                        break;
                    }
                }
            }
        }
        return new java.util.ArrayList<>(stateful);
    }

    /** Record an Assignment statement's LHS-name -> RHS-expression mapping. */
    private static void collectAssignment(EObject stmt, java.util.Map<String, EObject> defs) {
        if (!"Assignment".equals(stmt.eClass().getName())) return;
        EObject left = refFeature(stmt, "left");
        EObject right = refFeature(stmt, "right");
        if (left == null || right == null) return;
        // VarRef.name is a reference to the Variable; its own name attribute
        // is the string the template keys on.
        String lhsName = null;
        Object nameVal = featureValue(left, "name");
        if (nameVal instanceof EObject varObj) {
            lhsName = stringFeature(varObj, "name");
        } else if (nameVal instanceof String sVal) {
            lhsName = sVal;
        }
        if (lhsName != null) defs.put(lhsName, right);
    }

    /**
     * Collect every name READ by an expression tree: zero-arg CallExp function
     * names (the ETL's shape for state-var and sensor reads — argument-taking
     * calls contribute their name too, harmlessly: declared functions are
     * never state variables) and RefExp referenced-variable names. Mirrors the
     * template's collectLensReadNames.
     */
    private static void collectReadNames(EObject expr, java.util.Set<String> acc) {
        visitForReads(expr, acc);
        for (var it = expr.eAllContents(); it.hasNext(); ) {
            visitForReads(it.next(), acc);
        }
    }

    private static void visitForReads(EObject o, java.util.Set<String> acc) {
        String cls = o.eClass().getName();
        if ("CallExp".equals(cls)) {
            EObject fn = refFeature(o, "function");
            if (fn != null && "StringExp".equals(fn.eClass().getName())) {
                Object v = featureValue(fn, "value");
                if (v != null) acc.add(v.toString());
            }
        } else if ("RefExp".equals(cls)) {
            EObject ref = refFeature(o, "ref");
            if (ref != null) {
                String rn = stringFeature(ref, "name");
                if (rn != null) acc.add(rn);
            }
        }
    }

    private static Object featureValue(EObject o, String feature) {
        EStructuralFeature f = o.eClass().getEStructuralFeature(feature);
        return f == null ? null : o.eGet(f);
    }

    private static String stringFeature(EObject o, String feature) {
        Object v = featureValue(o, feature);
        return v == null ? null : v.toString();
    }

    private static EObject refFeature(EObject o, String feature) {
        Object v = featureValue(o, feature);
        return (v instanceof EObject e) ? e : null;
    }

    @SuppressWarnings("unchecked")
    private static java.util.List<Object> listFeature(EObject o, String feature) {
        Object v = featureValue(o, feature);
        return (v instanceof java.util.List) ? (java.util.List<Object>) v : java.util.List.of();
    }

    /**
     * Walk the resource to find the first StateMachineDef's name.
     */
    /** Every StateMachineDef name, in the order of its package's {@code machines} list. */
    private static java.util.List<String> findStateMachineNames(Resource rcResource) {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (var it = rcResource.getAllContents(); it.hasNext(); ) {
            EObject obj = it.next();
            EStructuralFeature mf = obj.eClass().getEStructuralFeature("machines");
            if (mf == null || !"RCPackage".equals(obj.eClass().getName())) continue;
            for (Object m : (java.util.List<?>) obj.eGet(mf)) {
                String n = stringFeature((EObject) m, "name");
                if (n != null) names.add(n);
            }
        }
        if (names.isEmpty()) {
            String first = findStateMachineName(rcResource);
            if (first != null) names.add(first);
        }
        return names;
    }

    private static String findStateMachineName(Resource rcResource) {
        for (var it = rcResource.getAllContents(); it.hasNext(); ) {
            EObject obj = it.next();
            if ("StateMachineDef".equals(obj.eClass().getName())) {
                EStructuralFeature nameFeature = obj.eClass().getEStructuralFeature("name");
                if (nameFeature != null) {
                    Object name = obj.eGet(nameFeature);
                    if (name != null) return name.toString();
                }
            }
        }
        return null;
    }

    /**
     * Write a minimal ROOT file declaring a session that inherits from
     * the CyPhyAssure {@code Z_Machines} heap. {@code isabelle build}
     * uses this to locate and verify the theory non-interactively.
     *
     * <p>Note: the parent session is named {@code Z_Machines} (plural),
     * declared in {@code Isabelle2023-CyPhyAssure/src/CyPhyAssure/Z_Machines/ROOT}.
     * The theory inside it (referenced from the generated .thy as
     * {@code imports "Z_Machines.Z_Machine"}) is singular, hence the
     * historical confusion.
     */
    private static void writeRootFile(Path isabelleDir, java.util.List<String> stmNames) throws IOException {
        // Session named after the first machine, exactly as before; with one
        // machine the file is byte-identical to the old single-theory form.
        StringBuilder content = new StringBuilder(
            "session " + stmNames.get(0) + "_Check = \"Z_Machines\" +\n" +
            "  theories\n");
        for (String n : stmNames) content.append("    ").append(n).append("_Beh\n");
        Path rootPath = isabelleDir.resolve("ROOT");
        Files.writeString(rootPath, content.toString());
    }
}
