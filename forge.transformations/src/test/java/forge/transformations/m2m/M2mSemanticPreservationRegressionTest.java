package forge.transformations.m2m;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import forge.transformations.t2m.SpoonDiscoverer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression guards for the M2M semantic-preservation defects diagnosed in
 * {@code review/semantic-preservation-audit-2026-08-31.md}.
 *
 * <p>Each test pins ONE defect and fails against the pre-fix
 * {@code java2robochart.etl}:
 *
 * <ul>
 *   <li>{@link #u3_computeStatementsFollowJavaSourceOrder()} — U3. Pass 2 keyed
 *       field assignments by name in a Map (dropping source order) and Pass 3
 *       re-emitted them in field-DECLARATION order, so an assignment reading a
 *       field an earlier statement writes saw the previous cycle's value.</li>
 *   <li>{@link #u3_repeatedAssignmentToSameFieldIsNotCollapsed()} — U3 corollary.
 *       The Map keying also collapsed {@code this.x = a; this.x = b;} to the
 *       last write only.</li>
 *   <li>{@link #u5_booleanFieldCarriesConstructorInitialValue()} — U5. The
 *       central witness: {@code Variable.initial} was never populated, so the
 *       CSP memory started {@code Memory_camActive(true)} where the Java
 *       constructor sets {@code false}.</li>
 *   <li>{@link #u5_negativeSentinelInitialIsSignCorrect()} — U5 + T2M-2
 *       interaction. {@code cstc = -1} must survive as {@code -1}: the
 *       persisted XMI stores NEG over CtLiteral("1") with no {@code value="-1"}
 *       anywhere, so a consumer that misses the unary wrapper recovers +1.</li>
 *   <li>{@link #u5_declarationInitialiserIsCarried()} — U5. Declaration-site
 *       initialisers, not just constructor assignments.</li>
 *   <li>{@link #u5_constructorOverridesDeclarationInitialiser()} — U5. Java
 *       runs the declaration initialiser first and the constructor body second,
 *       so the constructor value must win.</li>
 *   <li>{@link #u5_unresolvableInitialiserLeavesSlotAbsentRatherThanSubstituting()}
 *       — U5 safety property. {@code Double.MAX_VALUE} must NOT be silently
 *       replaced by a fabricated literal; the slot stays empty so the backend
 *       type default applies and the M2M log announces it.</li>
 *   <li>{@link #f1_eventTriggeredBranchHasNoDataGuardToContribute()} — S1
 *       BLOCKER witness. Documents WHY the prescribed gate relaxation at
 *       ETL:641/:4336 cannot close the C1 residual, and fails if that premise
 *       ever stops holding (i.e. if an event-triggered branch starts carrying a
 *       data guard, at which point the relaxation becomes both meaningful and
 *       necessary).</li>
 * </ul>
 */
class M2mSemanticPreservationRegressionTest {

    @TempDir
    Path tempDir;

    private SpoonDiscoverer discoverer;
    private Java2RoboChartTransformer transformer;

    @BeforeEach
    void setUp() {
        discoverer = new SpoonDiscoverer();
        transformer = new Java2RoboChartTransformer();
    }

    // ========================================================================
    // U3 — compute() statement order
    // ========================================================================

    /**
     * U3. Mirrors LRE {@code CalcCPA}: the field DECLARATION order is
     * {@code cda} then {@code tcpa}, but {@code compute()} assigns
     * {@code tcpa} first and then reads it when computing {@code cda}.
     *
     * <p>Pre-fix the emitted SeqStatement was [cda, tcpa] — declaration order —
     * so the model's {@code cda} consumed the PREVIOUS cycle's {@code tcpa},
     * a value the Java never uses at that point. Post-fix it is [tcpa, cda].
     */
    @Test
    void u3_computeStatementsFollowJavaSourceOrder() throws IOException {
        writeComputeOperation("""
                    public void compute() {
                        this.tcpa = sensor.a() + 1.0;
                        this.cda = this.tcpa * 2.0;
                    }
                """, "    private double cda;\n    private double tcpa;\n");

        List<String> order = computeAssignmentOrder();

        assertEquals(List.of("tcpa", "cda"), order,
                "U3: compute() statements must be emitted in JAVA SOURCE order. "
                + "Declaration order here is [cda, tcpa]; emitting that order makes "
                + "cda read the previous cycle's tcpa.");
    }

    /**
     * U3 corollary. Keying assignments by field name in a Map silently
     * collapsed repeated writes to the same field; the source-order Sequence
     * records each one.
     */
    @Test
    void u3_repeatedAssignmentToSameFieldIsNotCollapsed() throws IOException {
        writeComputeOperation("""
                    public void compute() {
                        this.cda = 1.0;
                        this.tcpa = this.cda + 1.0;
                        this.cda = this.tcpa * 2.0;
                    }
                """, "    private double cda;\n    private double tcpa;\n");

        List<String> order = computeAssignmentOrder();

        assertEquals(List.of("cda", "tcpa", "cda"), order,
                "U3: each field assignment in compute() must be emitted, in source "
                + "order. Map-by-name keying kept only the last write to cda.");
    }

    // ========================================================================
    // U5 — initial data state
    // ========================================================================

    /**
     * U5 central witness. {@code camActive} is set {@code false} by the
     * constructor; pre-fix no {@code initial} was emitted at all and the CSP
     * memory started it at {@code true} — a state the Java cannot produce.
     */
    @Test
    void u5_booleanFieldCarriesConstructorInitialValue() throws IOException {
        writeControllerWithFields(
                "    private boolean camActive;\n",
                "        this.camActive = false;\n");

        EObject initial = ctrlStateInitial("camActive");

        assertNotNull(initial,
                "U5: Variable.initial must be populated from the Java constructor. "
                + "With it absent the CSP memory starts Memory_camActive(true), "
                + "which the Java constructor never produces.");
        assertEquals("BooleanExp", initial.eClass().getName());
        assertEquals("false", String.valueOf(get(initial, "value")));
    }

    /**
     * U5 + T2M-2. The negative sentinel must keep its sign. The persisted
     * discovered_model.xmi holds CtUnaryOperator[NEG] over CtLiteral("1") and
     * contains no {@code value="-1"} at all, so a resolver that misses the
     * unary wrapper silently recovers +1.
     */
    @Test
    void u5_negativeSentinelInitialIsSignCorrect() throws IOException {
        writeControllerWithFields(
                "    private int cstc;\n",
                "        this.cstc = -1;\n");

        EObject initial = ctrlStateInitial("cstc");

        assertNotNull(initial, "U5: negative sentinel initial must be emitted");
        assertEquals("IntegerExp", initial.eClass().getName());
        assertEquals(-1, ((Number) get(initial, "value")).intValue(),
                "U5: the sentinel must survive as -1, not +1 (unary-minus wrapper) "
                + "and not 0 (type default).");
    }

    /** U5. Declaration-site initialisers are carried too, not just constructor ones. */
    @Test
    void u5_declarationInitialiserIsCarried() throws IOException {
        writeControllerWithFields(
                "    private boolean armed = true;\n",
                "");

        EObject initial = ctrlStateInitial("armed");

        assertNotNull(initial, "U5: declaration initialiser must be carried");
        assertEquals("true", String.valueOf(get(initial, "value")));
    }

    /**
     * U5 ordering. Java runs the declaration initialiser first and the
     * constructor body afterwards, so the constructor's value is the one the
     * object actually starts with.
     */
    @Test
    void u5_constructorOverridesDeclarationInitialiser() throws IOException {
        writeControllerWithFields(
                "    private int mark = 7;\n",
                "        this.mark = 3;\n");

        EObject initial = ctrlStateInitial("mark");

        assertNotNull(initial);
        assertEquals(3, ((Number) get(initial, "value")).intValue(),
                "U5: the constructor body overwrites the declaration initialiser in "
                + "Java, so 3 (not 7) is the initial data state.");
    }

    /**
     * U5 safety property. A value the stage cannot fold must NOT be replaced
     * by a fabricated literal — the slot stays absent so the backend type
     * default applies and the substitution stays visible.
     */
    @Test
    void u5_unresolvableInitialiserLeavesSlotAbsentRatherThanSubstituting() throws IOException {
        writeControllerWithFields(
                "    private double cda;\n",
                "        this.cda = Double.MAX_VALUE;\n");

        EObject initial = ctrlStateInitial("cda");

        assertNull(initial,
                "U5: an initialiser that does not fold to a literal must leave "
                + "Variable.initial ABSENT. Emitting a made-up value here would be "
                + "worse than the missing initial: it would assert a starting state "
                + "the Java does not have.");
    }

    // ========================================================================
    // S1 — the C1 priority-gate BLOCKER
    // ========================================================================

    /**
     * S1 blocker witness, in the exact shape of LRE's CAM block:
     * an event-triggered branch ({@code reqOCM}) followed by a triggerless
     * guarded branch ({@code cdaAboveOrAtMinSafe}).
     *
     * <p>The prescribed fix was to relax the {@code eventTypeName == ""} gate
     * at ETL:641/:4336 so an event-triggered branch contributes a blocking
     * conjunct to later non-event-triggered branches. This test pins the reason
     * that relaxation cannot work: the earlier branch's guard-conjunct list is
     * EMPTY — its only firing condition is the presence of the event — so there
     * is no data predicate to negate. The gate is not what suppresses the
     * conjunct; there is no conjunct.
     *
     * <p>Expressing the real blocking condition needs "reqOCM is NOT offered",
     * and {@code robochart.ecore} has no event-absence expression: the
     * {@code Communication} metaclass (:374-383) models only a positive trigger
     * on {@code Transition.trigger}, and no {@code Expression} subclass ranges
     * over the trigger alphabet.
     *
     * <p>The assertion is deliberately on the SHAPE rather than on an emitted
     * negation: if a future case study produces an event-triggered branch that
     * DOES carry a data guard, this test fails and signals that the gate
     * relaxation has become both meaningful and required.
     */
    @Test
    void f1_eventTriggeredBranchHasNoDataGuardToContribute() throws IOException {
        writeSource("f1", "CamMode.java", """
                package f1;
                public enum CamMode { CAM, OCM }
                """);
        writeSource("f1", "CamEvent.java", """
                package f1;
                public sealed interface CamEvent {
                    record ReqOCM() implements CamEvent {}
                }
                """);
        writeSource("f1", "CamController.java", """
                package f1;
                public final class CamController {
                    private CamMode currentMode = CamMode.CAM;
                    private double cda;

                    public CamController() {
                        this.cda = 0.0;
                    }

                    public void step(CamEvent event) {
                        boolean cdaAboveOrAtMinSafe = cda >= 1.0;
                        if (currentMode == CamMode.CAM) {
                            if (event instanceof CamEvent.ReqOCM) {
                                currentMode = CamMode.OCM;
                            } else if (cdaAboveOrAtMinSafe) {
                                currentMode = CamMode.OCM;
                            }
                        }
                    }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, discoverer.getResolvedValues());
        EObject stm = getChild(rcResource.getContents().get(0), "machines", 0);

        EObject eventTriggered = null;
        for (EObject t : getChildren(stm, "transitions")) {
            if (get(t, "trigger") != null && isFromState(t, stm, "CAM")) {
                eventTriggered = t;
            }
        }
        assertNotNull(eventTriggered,
                "fixture must produce the event-triggered CAM branch");

        assertNull(get(eventTriggered, "condition"),
                "BLOCKER premise: the event-triggered branch carries NO data "
                + "guard, so relaxing the eventTypeName gate at ETL:641/:4336 has "
                + "nothing to contribute as a blocking conjunct. If this assertion "
                + "ever fails, an event-triggered branch has acquired a data guard "
                + "and the gate relaxation becomes both meaningful and necessary.");
    }

    // ========================================================================
    // Fixtures
    // ========================================================================

    /** Controller with a compute()-style Operation class alongside it. */
    private void writeComputeOperation(String computeBody, String fields) throws IOException {
        writeSource("cmp", "CmpMode.java", """
                package cmp;
                public enum CmpMode { A, B }
                """);
        writeSource("cmp", "CmpEvent.java", """
                package cmp;
                public sealed interface CmpEvent {
                    record Go() implements CmpEvent {}
                }
                """);
        writeSource("cmp", "Sensor.java", """
                package cmp;
                public final class Sensor {
                    public double a() { return 0.0; }
                }
                """);
        writeSource("cmp", "CalcThing.java",
                "package cmp;\n"
                + "public final class CalcThing {\n"
                + "    private final Sensor sensor;\n"
                + fields
                + "    public CalcThing(Sensor sensor) { this.sensor = sensor; }\n"
                + computeBody
                + "    public double cda() { return cda; }\n"
                + "    public double tcpa() { return tcpa; }\n"
                + "}\n");
        writeSource("cmp", "CmpController.java", """
                package cmp;
                public final class CmpController {
                    private CmpMode currentMode = CmpMode.A;
                    private final CalcThing calcThing;

                    public CmpController(CalcThing calcThing) {
                        this.calcThing = calcThing;
                    }

                    public void step(CmpEvent event) {
                        calcThing.compute();
                        if (currentMode == CmpMode.A) {
                            if (event instanceof CmpEvent.Go) {
                                currentMode = CmpMode.B;
                            }
                        }
                    }
                }
                """);
    }

    /** Controller carrying the given Ctrl_State fields and constructor body. */
    private void writeControllerWithFields(String fields, String ctorBody) throws IOException {
        writeSource("st", "StMode.java", """
                package st;
                public enum StMode { A, B }
                """);
        writeSource("st", "StEvent.java", """
                package st;
                public sealed interface StEvent {
                    record Go() implements StEvent {}
                }
                """);
        writeSource("st", "StController.java",
                "package st;\n"
                + "public final class StController {\n"
                + "    private StMode currentMode = StMode.A;\n"
                + fields
                + "    public StController() {\n"
                + ctorBody
                + "    }\n"
                + "    public void step(StEvent event) {\n"
                + "        if (currentMode == StMode.A) {\n"
                + "            if (event instanceof StEvent.Go) {\n"
                + "                currentMode = StMode.B;\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
    }

    /** Names of the assignment LHS variables in the emitted OperationDef body, in order. */
    private List<String> computeAssignmentOrder() throws IOException {
        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, discoverer.getResolvedValues());
        EObject rcPackage = rcResource.getContents().get(0);

        EObject calcOp = null;
        for (EObject op : getChildren(rcPackage, "operations")) {
            if ("CalcThing".equals(get(op, "name"))) {
                calcOp = op;
            }
        }
        assertNotNull(calcOp, "fixture must emit the CalcThing OperationDef");

        EObject body = null;
        for (EObject t : getChildren(calcOp, "transitions")) {
            if (get(t, "action") != null) {
                body = (EObject) get(t, "action");
            }
        }
        assertNotNull(body, "CalcThing must have an extracted body (not stubs)");

        List<String> names = new ArrayList<>();
        collectAssignmentTargets(body, names);
        return names;
    }

    private static void collectAssignmentTargets(EObject stmt, List<String> out) {
        if (stmt == null) {
            return;
        }
        String kind = stmt.eClass().getName();
        if ("SeqStatement".equals(kind)) {
            for (EObject s : getChildren(stmt, "statements")) {
                collectAssignmentTargets(s, out);
            }
            return;
        }
        if ("Assignment".equals(kind)) {
            EObject lhs = (EObject) get(stmt, "left");
            if (lhs != null) {
                EObject named = (EObject) get(lhs, "name");
                if (named != null) {
                    out.add((String) get(named, "name"));
                }
            }
        }
    }

    /** The {@code initial} Expression of the named Ctrl_State variable, or null. */
    private EObject ctrlStateInitial(String varName) throws IOException {
        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, discoverer.getResolvedValues());
        EObject rcPackage = rcResource.getContents().get(0);

        for (EObject iface : getChildren(rcPackage, "interfaces")) {
            if (!"Ctrl_State".equals(get(iface, "name"))) {
                continue;
            }
            for (EObject vl : getChildren(iface, "variableList")) {
                for (EObject v : getChildren(vl, "vars")) {
                    if (varName.equals(get(v, "name"))) {
                        return (EObject) get(v, "initial");
                    }
                }
            }
        }
        fail("Ctrl_State variable '" + varName + "' not found in the emitted model");
        return null;
    }

    private boolean isFromState(EObject transition, EObject stm, String stateName) {
        EObject src = (EObject) get(transition, "source");
        return src != null && stateName.equals(get(src, "name"));
    }

    private void writeSource(String packageDir, String fileName, String source) throws IOException {
        Path dir = tempDir.resolve(packageDir);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(fileName), source);
    }

    private static Object get(EObject obj, String featureName) {
        EStructuralFeature f = obj.eClass().getEStructuralFeature(featureName);
        return f != null ? obj.eGet(f) : null;
    }

    @SuppressWarnings("unchecked")
    private static List<EObject> getChildren(EObject obj, String featureName) {
        Object val = get(obj, featureName);
        return val instanceof List<?> list ? (List<EObject>) list : List.of();
    }

    private static EObject getChild(EObject obj, String featureName, int index) {
        return getChildren(obj, featureName).get(index);
    }
}
