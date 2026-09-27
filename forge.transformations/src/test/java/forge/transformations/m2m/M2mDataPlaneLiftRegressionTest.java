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
 * Regression guards for the I1-remainder and I2 defects in
 * {@code java2robochart.etl}.
 *
 * <p>Each test pins ONE behaviour and FAILS against the pre-fix ETL. Every
 * assertion is about the emitted RoboChart model — nothing here claims that
 * any Isabelle theory elaborates, that any proof closes, or that FDR4 accepts
 * any refinement; none of those tools is runnable in this environment.
 *
 * <ul>
 *   <li>{@link #i2_operationBodyFunctionSignatureIsInferredFromJavaDeclaredTypes()}
 *       — I2, the central witness. Phase 6b declared operation-body-discovered
 *       functions with a hardcoded {@code (x : real) : real} and performed no
 *       Java type inference, so LRE emitted
 *       {@code function nsRelDist ( x : real ) : real} against
 *       {@code double nsRelDist(int index)}. Pre-fix this was inert (those
 *       functions were applied nowhere); the I1 data-plane inlining applies
 *       them, which makes the wrong domain live.</li>
 *   <li>{@link #i2_guardPathSignatureInferenceIsUnchanged()} — I2 no-regression.
 *       The inference body was EXTRACTED from the guard path into a shared
 *       operation; the guard path must still infer exactly as before.</li>
 *   <li>{@link #i2_functionWithNoResolvableJavaMethodKeepsTheRealFallback()}
 *       — I2 boundary. {@code sqrt} resolves to {@code java.lang.Math} and is
 *       absent from the Spoon model, so the previous {@code (x : real) : real}
 *       must survive rather than becoming a guess.</li>
 *   <li>{@link #i1_controllerLevelFieldAssignmentIsLifted()} — I1 remainder,
 *       the central witness. A {@code this.<field> = <expr>} at controller top
 *       level reached NEITHER an OperationDef action (only compute() bodies did)
 *       NOR a guard (only controller LOCALS did), so its defining expression was
 *       dropped and the variable arrived in the theory as a lens no operation
 *       assigns.</li>
 *   <li>{@link #i1_liftedAssignmentsCarryJavaSourceOrder()} — I1 remainder, the
 *       U3 lesson. The lifted block must carry Java source order, not field
 *       declaration order, so a downstream consumer that inlines dependencies
 *       resolves a read to the fresh definition.</li>
 *   <li>{@link #i1_identityCopyOutIsNotLifted()} — I1 remainder safety.
 *       {@code this.vel = calcVel.vel()} renders as {@code vel = vel}; emitting
 *       it would add a vacuous clause and misattribute the definition.</li>
 *   <li>{@link #i1_assignmentReadingAStubbedOperationOutputIsNotLifted()} — I1
 *       remainder safety, the SRanger case. Lifting
 *       {@code turnDurationElapsed = elapsed} where {@code elapsed} is stubbed
 *       to {@code false} would PIN the predicate false, which is strictly worse
 *       than leaving the lens frozen-but-arbitrary.</li>
 *   <li>{@link #i1sr_partialComputeBodyStubsOnlyTheUnassignedOutput()} — I1-SR
 *       central witness. Pass 3a's WHOLE-BODY completeness check stubbed every
 *       output of a class when any one declared output went unassigned, so an
 *       output with a translatable RHS lost it. Classification is now
 *       per-variable. FAILS pre-change on {@code elapsed}'s RHS kind.</li>
 *   <li>{@link #i1sr_outputReadingAnUnassignedOutputIsTransitivelyStubbed()}
 *       — I1-SR soundness boundary, and the reason per-variable stubbing does
 *       NOT close SRanger's lens. When an assigned output's RHS READS a stubbed
 *       one (SRanger's real shape: {@code elapsed} reads {@code resetTime}),
 *       emitting the real expression would splice the placeholder in as if it
 *       were the Java's value, so the closure stubs it too.</li>
 *   <li>{@link #i1_controllerWithNoTopLevelFieldAssignmentEmitsNoRefreshOperation()}
 *       — I1 remainder. Both a no-op property AND the guard on Epsilon's
 *       {@code Sequence{0..-1}} (which is NOT empty), whose absence aborted the
 *       entire M2M phase on the chemical detector study.</li>
 *   <li>{@link #i1_guardsAreUnchangedByLifting()} — C1 no-regression. The
 *       priority-negation synthesis reads controller-level locals as guard
 *       predicates; lifting field assignments must not perturb any emitted
 *       guard.</li>
 * </ul>
 */
class M2mDataPlaneLiftRegressionTest {

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
    // I2 — function signature inference for operation-body functions
    // ========================================================================

    /**
     * I2 central witness. {@code relDist} is discovered ONLY from the
     * operation BODY (the compute() assignment calls it); no guard mentions it.
     * Its Java declaration is {@code double relDist(int index)}, so the emitted
     * RoboChart Function must be {@code (index : int) : real}.
     *
     * <p>Pre-fix Phase 6b hardcoded {@code (x : real) : real} here, so this
     * test fails on BOTH the parameter name and the parameter type.
     */
    @Test
    void i2_operationBodyFunctionSignatureIsInferredFromJavaDeclaredTypes() throws IOException {
        writeOpBodyFunctionFixture();
        EObject fn = functionNamed(rcPackage(), "relDist");
        assertNotNull(fn, "fixture must surface relDist as a declared Function");

        List<EObject> params = getChildren(fn, "parameters");
        assertEquals(1, params.size(), "relDist takes exactly one parameter");
        assertEquals("index", get(params.get(0), "name"),
                "I2: the parameter NAME must come from the Java declaration "
                + "(`int index`), not the hardcoded placeholder `x`");
        assertEquals("int", typeName(get(params.get(0), "type")),
                "I2: the parameter TYPE must be inferred from the Java DECLARED "
                + "type `int`, not the hardcoded `real`. Pre-fix Phase 6b did no "
                + "inference at all for operation-body-discovered functions.");
        assertEquals("real", typeName(get(fn, "type")),
                "return type must be inferred from Java `double`");
    }

    /**
     * I2 no-regression. {@code guardDist} is discovered from a GUARD, the path
     * that already inferred correctly. The inference body was moved into a
     * shared operation, so this pins that the move was behaviour-preserving.
     */
    @Test
    void i2_guardPathSignatureInferenceIsUnchanged() throws IOException {
        writeOpBodyFunctionFixture();
        EObject fn = functionNamed(rcPackage(), "guardDist");
        assertNotNull(fn, "fixture must surface guardDist as a declared Function");

        List<EObject> params = getChildren(fn, "parameters");
        assertEquals(1, params.size());
        assertEquals("idx", get(params.get(0), "name"),
                "guard-path inference must still take the Java parameter name");
        assertEquals("int", typeName(get(params.get(0), "type")),
                "guard-path inference must still map Java int -> RoboChart int");
        assertEquals("real", typeName(get(fn, "type")));
    }

    /**
     * I2 boundary. A function name with NO resolvable Java method must keep the
     * previous {@code (x : real) : real} fallback rather than acquiring an
     * invented signature. {@code Math.sqrt} is the real-world instance: it is
     * not part of the discovered Spoon model.
     */
    @Test
    void i2_functionWithNoResolvableJavaMethodKeepsTheRealFallback() throws IOException {
        writeOpBodyFunctionFixture();
        EObject fn = functionNamed(rcPackage(), "sqrt");
        assertNotNull(fn, "fixture must surface sqrt as a declared Function");

        List<EObject> params = getChildren(fn, "parameters");
        assertEquals(1, params.size());
        assertEquals("x", get(params.get(0), "name"),
                "with no Java method to read, the placeholder name is correct");
        assertEquals("real", typeName(get(params.get(0), "type")));
        assertEquals("real", typeName(get(fn, "type")));
    }

    // ========================================================================
    // I1 remainder — controller-level field assignment lifting
    // ========================================================================

    /**
     * I1 remainder, central witness. Mirrors LRE
     * {@code this.camActive = this.cda < MIN && this.tcpa >= 0.0}: a compound
     * predicate FIELD assigned at controller top level, read by a guard.
     *
     * <p>Pre-fix the ETL lifted compute() bodies into OperationDef actions and
     * controller-level LOCALS into guards, but controller-level FIELD
     * assignments into neither — so no artefact carried the expression and the
     * variable reached the theory as an unassigned (hence run-constant) lens.
     * Post-fix a {@code <Controller>_Refresh} OperationDef carries it.
     */
    @Test
    void i1_controllerLevelFieldAssignmentIsLifted() throws IOException {
        writeCtrlFieldFixture();
        List<String> lifted = refreshAssignmentOrder("CtlController_Refresh");
        assertTrue(lifted.contains("camActive"),
                "I1: the controller-level field assignment `this.camActive = ...` "
                + "must be lifted so its defining expression exists in the model. "
                + "Pre-fix it reached neither an operation action nor a guard. "
                + "Lifted names were: " + lifted);
    }

    /**
     * I1 remainder, the U3 lesson. {@code step()} assigns {@code first} and then
     * {@code second}, where {@code second} READS {@code first}; the field
     * DECLARATION order is the reverse. The lifted block must be in Java source
     * order, so a downstream consumer inlining dependencies resolves the read to
     * the fresh definition rather than a stale one.
     */
    @Test
    void i1_liftedAssignmentsCarryJavaSourceOrder() throws IOException {
        writeCtrlFieldFixture();
        List<String> lifted = refreshAssignmentOrder("CtlController_Refresh");
        int iFirst = lifted.indexOf("first");
        int iSecond = lifted.indexOf("second");
        assertTrue(iFirst >= 0 && iSecond >= 0,
                "both order-witness fields must be lifted; got " + lifted);
        assertTrue(iFirst < iSecond,
                "I1/U3: `first` is assigned BEFORE `second` in step() and "
                + "`second` reads it, but the field declarations are in the "
                + "opposite order. Emitting declaration order would make the "
                + "model's `second` read a stale `first`. Got: " + lifted);
    }

    /**
     * I1 remainder safety. {@code this.vel = calcVel.vel()} is a copy-out whose
     * RHS renders as a bare read of the SAME name, because the operation output
     * variable and the controller field share it. Lifting it would emit
     * {@code vel = vel} — vacuous, and it would misattribute the definition to
     * the controller when the operation defines it.
     */
    @Test
    void i1_identityCopyOutIsNotLifted() throws IOException {
        writeCtrlFieldFixture();
        List<String> lifted = refreshAssignmentOrder("CtlController_Refresh");
        assertFalse(lifted.contains("vel"),
                "I1: the identity copy-out `this.vel = calcVel.vel()` must NOT "
                + "be lifted (it renders as `vel = vel`). Got: " + lifted);
    }

    /**
     * I1 remainder safety — the SRanger case. The operation's compute() declares
     * an output it never assigns, so extractComputeBody bails out and BOTH
     * outputs become stubs ({@code elapsed = false}). The controller's
     * {@code this.flag = timer.elapsed()} therefore renders as
     * {@code flag = elapsed} where {@code elapsed} is a placeholder. Lifting it
     * would PIN {@code flag} to the placeholder — a definite wrong value —
     * rather than leaving the lens unconstrained. It must be refused.
     */
    @Test
    void i1_assignmentReadingAStubbedOperationOutputIsNotLifted() throws IOException {
        writeTransitiveStubFixture();
        EObject pkg = rcPackage();

        // The operation's body must indeed be stubs, else the fixture is not
        // exercising the intended path. Under per-variable stubbing this holds
        // for a DIFFERENT and more precise reason than it used to: `resetTime`
        // is stubbed because compute() does not assign it, and `elapsed` is
        // stubbed because its RHS READS `resetTime` (the transitive closure),
        // not merely because the body was declared incomplete as a whole.
        List<String> stubRhs = operationAssignmentRhsKinds("Timer");
        assertEquals(List.of("IntegerExp", "BooleanExp"), stubRhs,
                "fixture premise: BOTH of Timer's outputs must be stubs — "
                + "`resetTime` unassigned, `elapsed` transitively tainted by "
                + "reading it; got RHS kinds " + stubRhs);

        assertNull(operationNamed(pkg, "StbController_Refresh"),
                "I1: every candidate assignment reads a stubbed operation "
                + "output, so no refresh operation may be synthesized — lifting "
                + "would propagate the stub placeholder as if it were the "
                + "Java's value.");
    }

    /**
     * I1-SR central witness. PER-VARIABLE stubbing of a partial compute() body.
     *
     * <p>{@code Timer} declares two outputs — {@code resetTime} and
     * {@code elapsed}, both private fields with zero-arg getters — but
     * {@code compute()} assigns only {@code elapsed}, and its RHS reads NOTHING
     * stubbed. So the two outputs must be classified independently:
     * {@code resetTime} stubbed, {@code elapsed} carrying its real translated
     * expression.
     *
     * <p>PRE-CHANGE THIS FAILS. Pass 3a was a WHOLE-BODY completeness check: any
     * unassigned declared output aborted extraction and the caller stubbed
     * EVERY output, so {@code elapsed} arrived as the literal {@code false}
     * despite having a perfectly translatable RHS. The assertion on
     * {@code elapsed}'s RHS kind is the one that flips.
     *
     * <p>And because {@code elapsed} is then no longer in
     * {@code stubbedOpVarNames}, the pre-existing Phase 6a-ctrl guard stops
     * refusing {@code this.flag = timer.elapsed()} — so the controller-level
     * lift follows without any change to that pass. That consequence is
     * asserted here too, since it is the reason the change was made.
     */
    @Test
    void i1sr_partialComputeBodyStubsOnlyTheUnassignedOutput() throws IOException {
        writeStubbedOutputFixture();
        EObject pkg = rcPackage();

        EObject timer = operationNamed(pkg, "Timer");
        assertNotNull(timer, "fixture must emit the Timer OperationDef");

        List<String> targets = new ArrayList<>();
        List<String> kinds = new ArrayList<>();
        for (EObject t : getChildren(timer, "transitions")) {
            collectAssignmentTargets((EObject) get(t, "action"), targets);
            collectAssignmentRhsKinds((EObject) get(t, "action"), kinds);
        }

        int iReset = targets.indexOf("resetTime");
        int iElapsed = targets.indexOf("elapsed");
        assertTrue(iReset >= 0 && iElapsed >= 0,
                "both declared outputs must be assigned in the emitted body; "
                + "got targets " + targets);

        // The unassigned output is stubbed: `resetTime = 0` (IntegerExp, the
        // createAssignment default for a non-boolean).
        assertEquals("IntegerExp", kinds.get(iReset),
                "I1-SR: `resetTime` is declared an output but compute() does "
                + "not assign it, so it must be stubbed INDIVIDUALLY.");

        // The assigned output keeps its REAL expression. `sensor.reading() >= 2.0`
        // translates to a GreaterOrEqual — emphatically not a BooleanExp literal.
        assertEquals("GreaterOrEqual", kinds.get(iElapsed),
                "I1-SR: `elapsed` IS assigned in compute() and its RHS reads "
                + "nothing stubbed, so it must carry the translated Java "
                + "expression. Pre-change the whole-body completeness abort "
                + "stubbed it to BooleanExp(false) because a DIFFERENT output "
                + "(`resetTime`) was unassigned.");

        // Downstream consequence: the controller-level lift is no longer
        // refused, because `elapsed` is no longer a stubbed name.
        EObject refresh = operationNamed(pkg, "StbController_Refresh");
        assertNotNull(refresh,
                "I1-SR: with `elapsed` carrying a real expression it leaves "
                + "`stubbedOpVarNames`, so Phase 6a-ctrl's stub guard no longer "
                + "refuses `this.flag = timer.elapsed()` and the refresh "
                + "operation must be synthesized.");
        assertEquals(List.of("flag"), refreshAssignmentOrder("StbController_Refresh"),
                "the lifted block must carry exactly the controller field");
    }

    /**
     * I1-SR soundness boundary — the reason per-variable stubbing alone does NOT
     * close the SRanger lens, pinned as a test so it cannot regress silently.
     *
     * <p>Same fixture shape as above except that {@code elapsed}'s RHS READS the
     * unassigned output: {@code sensor.reading() - this.resetTime >= 2.0}, which
     * is SRanger's actual {@code TurnTimer.compute()} shape. Emitting
     * {@code elapsed}'s real expression here would place the stub literal
     * {@code 0} where the Java has the last reset timestamp, and the Isabelle
     * template's data-plane inlining would substitute it into the lifted
     * predicate — asserting "elapsed since the epoch" as the program's
     * semantics. That is a substituted value, not an under-approximation.
     *
     * <p>So the transitive closure must stub {@code elapsed} too, and the
     * controller lift must stay refused.
     */
    @Test
    void i1sr_outputReadingAnUnassignedOutputIsTransitivelyStubbed() throws IOException {
        writeTransitiveStubFixture();
        EObject pkg = rcPackage();

        EObject timer = operationNamed(pkg, "Timer");
        assertNotNull(timer, "fixture must emit the Timer OperationDef");

        List<String> targets = new ArrayList<>();
        List<String> kinds = new ArrayList<>();
        for (EObject t : getChildren(timer, "transitions")) {
            collectAssignmentTargets((EObject) get(t, "action"), targets);
            collectAssignmentRhsKinds((EObject) get(t, "action"), kinds);
        }

        assertEquals("BooleanExp", kinds.get(targets.indexOf("elapsed")),
                "I1-SR: `elapsed` is assigned in compute() but its RHS reads "
                + "`resetTime`, which compute() does not assign and which is "
                + "therefore stubbed to 0. Emitting the real expression would "
                + "splice that placeholder in as if it were the Java's reset "
                + "timestamp, so `elapsed` must be stubbed too.");

        assertNull(operationNamed(pkg, "StbController_Refresh"),
                "the controller lift must stay refused while `elapsed` is a "
                + "stub — this is SRanger's residual blocker, and it is "
                + "`resetTime` (written from a transition, absent from the data "
                + "plane), not the whole-body completeness check.");
    }

    /**
     * I1 remainder. A controller whose {@code step()} assigns fields only INSIDE
     * the if-else chain (they are transition actions, already handled) must
     * yield no refresh operation.
     *
     * <p>This also pins the empty-range guard: Epsilon's
     * {@code Sequence{0..-1}} is NOT empty, so without an explicit size check
     * the zero-candidate case indexed a zero-length sequence and aborted the
     * whole M2M phase — which is exactly what happened on the chemical detector
     * study, whose controllers have no top-level field assignments at all.
     */
    @Test
    void i1_controllerWithNoTopLevelFieldAssignmentEmitsNoRefreshOperation() throws IOException {
        writeNoTopLevelAssignmentFixture();
        EObject pkg = rcPackage();   // must not throw
        assertNull(operationNamed(pkg, "NopController_Refresh"),
                "no top-level field assignment means no refresh operation");
        assertFalse(getChildren(pkg, "machines").isEmpty(),
                "the phase must still complete and emit the state machine "
                + "(the empty-range guard); pre-guard this aborted the M2M");
    }

    /**
     * C1 no-regression. The priority-negation synthesis reads controller-level
     * LOCALS as guard predicates. This fixture has both a local-derived guard
     * (which C1 negates onto the later branch) and top-level field assignments
     * (which lifting now consumes). The emitted guards must be unaffected.
     */
    @Test
    void i1_guardsAreUnchangedByLifting() throws IOException {
        writeCtrlFieldFixture();
        EObject stm = getChild(rcPackage(), "machines", 0);

        EObject guarded = null;
        for (EObject t : getChildren(stm, "transitions")) {
            if (get(t, "condition") != null) {
                guarded = t;
            }
        }
        assertNotNull(guarded,
                "fixture must retain a guarded transition after lifting — if "
                + "lifting consumed the guard, C1 has regressed");

        List<String> names = new ArrayList<>();
        collectCallExpNames((EObject) get(guarded, "condition"), names);
        assertTrue(names.contains("camActive"),
                "C1: the guard must still read `camActive`. Lifting the "
                + "DEFINITION of a field must not remove or rewrite its guard "
                + "READ. Guard referenced: " + names);
    }

    // ========================================================================
    // Fixtures
    // ========================================================================

    /**
     * Operation whose compute() body calls {@code relDist(int)} and
     * {@code sqrt(double)}, plus a guard that calls {@code guardDist(int)}.
     * {@code relDist} is reachable ONLY from the operation body, so it exercises
     * the Phase 6b declaration path; {@code guardDist} only from the guard.
     */
    private void writeOpBodyFunctionFixture() throws IOException {
        writeSource("ob", "ObMode.java", """
                package ob;
                public enum ObMode { A, B }
                """);
        writeSource("ob", "ObEvent.java", """
                package ob;
                public sealed interface ObEvent {
                    record Go() implements ObEvent {}
                }
                """);
        writeSource("ob", "Sensor.java", """
                package ob;
                public final class Sensor {
                    public double relDist(int index) { return 0.0; }
                    public double guardDist(int idx) { return 0.0; }
                    public int closestIndex() { return 0; }
                }
                """);
        writeSource("ob", "CalcThing.java", """
                package ob;
                public final class CalcThing {
                    private final Sensor sensor;
                    private double dist;
                    public CalcThing(Sensor sensor) { this.sensor = sensor; }
                    public void compute() {
                        this.dist = Math.sqrt(sensor.relDist(sensor.closestIndex()));
                    }
                    public double dist() { return dist; }
                }
                """);
        writeSource("ob", "ObController.java", """
                package ob;
                public final class ObController {
                    private ObMode currentMode = ObMode.A;
                    private final CalcThing calcThing;
                    private final Sensor sensor;
                    private double dist;

                    public ObController(CalcThing calcThing, Sensor sensor) {
                        this.calcThing = calcThing;
                        this.sensor = sensor;
                    }

                    public void step(ObEvent event) {
                        calcThing.compute();
                        this.dist = calcThing.dist();
                        boolean farEnough = sensor.guardDist(sensor.closestIndex()) > 1.0;
                        if (currentMode == ObMode.A) {
                            if (farEnough) {
                                currentMode = ObMode.B;
                            }
                        }
                    }
                }
                """);
    }

    /**
     * Controller with THREE kinds of top-level field assignment: a compound
     * predicate (liftable), an identity copy-out (must be refused), and an
     * order-witness pair whose declaration order is the reverse of their
     * assignment order.
     */
    private void writeCtrlFieldFixture() throws IOException {
        writeSource("ctl", "CtlMode.java", """
                package ctl;
                public enum CtlMode { A, B, C }
                """);
        writeSource("ctl", "CtlEvent.java", """
                package ctl;
                public sealed interface CtlEvent {
                    record Go() implements CtlEvent {}
                }
                """);
        writeSource("ctl", "Sensor.java", """
                package ctl;
                public final class Sensor {
                    public double gap(int index) { return 0.0; }
                    public int closestIndex() { return 0; }
                }
                """);
        writeSource("ctl", "CalcVel.java", """
                package ctl;
                public final class CalcVel {
                    private final Sensor sensor;
                    private double vel;
                    public CalcVel(Sensor sensor) { this.sensor = sensor; }
                    public void compute() {
                        this.vel = sensor.gap(sensor.closestIndex());
                    }
                    public double vel() { return vel; }
                }
                """);
        // NOTE the field DECLARATION order: second BEFORE first. step() assigns
        // first then second, and second reads first.
        writeSource("ctl", "CtlController.java", """
                package ctl;
                public final class CtlController {
                    private CtlMode currentMode = CtlMode.A;
                    private final CalcVel calcVel;
                    private final Sensor sensor;
                    private double second;
                    private double first;
                    private double vel;
                    private boolean camActive;

                    public CtlController(CalcVel calcVel, Sensor sensor) {
                        this.calcVel = calcVel;
                        this.sensor = sensor;
                        this.camActive = false;
                    }

                    public void step(CtlEvent event) {
                        calcVel.compute();
                        this.vel = calcVel.vel();
                        this.first = sensor.gap(sensor.closestIndex());
                        this.second = this.first + 1.0;
                        this.camActive = this.vel < 1.0 && this.second >= 0.0;
                        boolean plainGuard = this.vel <= 2.0;
                        if (currentMode == CtlMode.A) {
                            if (camActive) {
                                currentMode = CtlMode.B;
                            } else if (plainGuard) {
                                currentMode = CtlMode.C;
                            }
                        }
                    }
                }
                """);
    }

    /**
     * Operation whose compute() assigns only ONE of its two declared outputs, so
     * extractComputeBody bails out and both become stubs; the controller then
     * copies the stubbed output into a field of a DIFFERENT name.
     */
    private void writeStubbedOutputFixture() throws IOException {
        writeSource("stb", "StbMode.java", """
                package stb;
                public enum StbMode { A, B }
                """);
        writeSource("stb", "StbEvent.java", """
                package stb;
                public sealed interface StbEvent {
                    record Go() implements StbEvent {}
                }
                """);
        writeSource("stb", "Sensor.java", """
                package stb;
                public final class Sensor {
                    public double reading() { return 0.0; }
                }
                """);
        // `resetTime` is declared and has a getter (so it IS an output
        // variable) but compute() never assigns it. `elapsed` IS assigned and
        // its RHS reads nothing stubbed, so per-variable classification must
        // stub only `resetTime` and let `elapsed` keep its real expression.
        // (Pre-change, Pass 3a's whole-body check stubbed both.)
        writeSource("stb", "Timer.java", """
                package stb;
                public final class Timer {
                    private final Sensor sensor;
                    private double resetTime;
                    private boolean elapsed;
                    public Timer(Sensor sensor) { this.sensor = sensor; }
                    public void compute() {
                        this.elapsed = sensor.reading() >= 2.0;
                    }
                    public boolean elapsed() { return elapsed; }
                    public double resetTime() { return resetTime; }
                }
                """);
        writeSource("stb", "StbController.java", """
                package stb;
                public final class StbController {
                    private StbMode currentMode = StbMode.A;
                    private final Timer timer;
                    private boolean flag;

                    public StbController(Timer timer) {
                        this.timer = timer;
                        this.flag = false;
                    }

                    public void step(StbEvent event) {
                        timer.compute();
                        this.flag = timer.elapsed();
                        if (currentMode == StbMode.A) {
                            if (flag) {
                                currentMode = StbMode.B;
                            }
                        }
                    }
                }
                """);
    }

    /**
     * SRanger's ACTUAL {@code TurnTimer} shape: {@code elapsed}'s RHS reads the
     * unassigned output {@code resetTime}. Identical to
     * {@link #writeStubbedOutputFixture()} except for that one read, which is
     * exactly what makes the transitive closure fire.
     */
    private void writeTransitiveStubFixture() throws IOException {
        writeSource("stb", "StbMode.java", """
                package stb;
                public enum StbMode { A, B }
                """);
        writeSource("stb", "StbEvent.java", """
                package stb;
                public sealed interface StbEvent {
                    record Go() implements StbEvent {}
                }
                """);
        writeSource("stb", "Sensor.java", """
                package stb;
                public final class Sensor {
                    public double reading() { return 0.0; }
                }
                """);
        writeSource("stb", "Timer.java", """
                package stb;
                public final class Timer {
                    private final Sensor sensor;
                    private double resetTime;
                    private boolean elapsed;
                    public Timer(Sensor sensor) { this.sensor = sensor; }
                    public void compute() {
                        this.elapsed = sensor.reading() - this.resetTime >= 2.0;
                    }
                    public boolean elapsed() { return elapsed; }
                    public double resetTime() { return resetTime; }
                }
                """);
        writeSource("stb", "StbController.java", """
                package stb;
                public final class StbController {
                    private StbMode currentMode = StbMode.A;
                    private final Timer timer;
                    private boolean flag;

                    public StbController(Timer timer) {
                        this.timer = timer;
                        this.flag = false;
                    }

                    public void step(StbEvent event) {
                        timer.compute();
                        this.flag = timer.elapsed();
                        if (currentMode == StbMode.A) {
                            if (flag) {
                                currentMode = StbMode.B;
                            }
                        }
                    }
                }
                """);
    }

    /** Controller whose only field assignments are INSIDE the if-else chain. */
    private void writeNoTopLevelAssignmentFixture() throws IOException {
        writeSource("nop", "NopMode.java", """
                package nop;
                public enum NopMode { A, B }
                """);
        writeSource("nop", "NopEvent.java", """
                package nop;
                public sealed interface NopEvent {
                    record Go(double v) implements NopEvent {}
                }
                """);
        writeSource("nop", "NopController.java", """
                package nop;
                public final class NopController {
                    private NopMode currentMode = NopMode.A;
                    private double payload;

                    public NopController() {
                        this.payload = 0.0;
                    }

                    public void step(NopEvent event) {
                        if (currentMode == NopMode.A) {
                            if (event instanceof NopEvent.Go g) {
                                this.payload = g.v();
                                currentMode = NopMode.B;
                            }
                        }
                    }
                }
                """);
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private EObject rcPackage() throws IOException {
        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, discoverer.getResolvedValues());
        return rcResource.getContents().get(0);
    }

    /** Assignment LHS names of the named refresh operation, in emitted order. */
    private List<String> refreshAssignmentOrder(String opName) throws IOException {
        EObject op = operationNamed(rcPackage(), opName);
        assertNotNull(op,
                "I1: no `" + opName + "` operation was synthesized, so no "
                + "controller-level field assignment was lifted at all");
        EObject body = null;
        for (EObject t : getChildren(op, "transitions")) {
            if (get(t, "action") != null) {
                body = (EObject) get(t, "action");
            }
        }
        assertNotNull(body, opName + " must carry an action");
        List<String> names = new ArrayList<>();
        collectAssignmentTargets(body, names);
        return names;
    }

    /** eClass names of the assignment RHSs in the named operation's body. */
    private List<String> operationAssignmentRhsKinds(String opName) throws IOException {
        EObject op = operationNamed(rcPackage(), opName);
        assertNotNull(op, "fixture must emit the " + opName + " OperationDef");
        List<String> kinds = new ArrayList<>();
        for (EObject t : getChildren(op, "transitions")) {
            collectAssignmentRhsKinds((EObject) get(t, "action"), kinds);
        }
        return kinds;
    }

    private static EObject operationNamed(EObject pkg, String name) {
        for (EObject op : getChildren(pkg, "operations")) {
            if (name.equals(get(op, "name"))) {
                return op;
            }
        }
        return null;
    }

    private static EObject functionNamed(EObject pkg, String name) {
        for (EObject f : getChildren(pkg, "functions")) {
            if (name.equals(get(f, "name"))) {
                return f;
            }
        }
        return null;
    }

    /** Resolve a TypeRef / PrimitiveType node to its RoboChart type name. */
    private static String typeName(Object type) {
        if (!(type instanceof EObject t)) {
            return null;
        }
        String kind = t.eClass().getName();
        if ("TypeRef".equals(kind)) {
            EObject ref = (EObject) get(t, "ref");
            return ref != null ? (String) get(ref, "name") : null;
        }
        if ("SeqType".equals(kind)) {
            return "Seq(" + typeName(get(t, "domain")) + ")";
        }
        return (String) get(t, "name");
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

    private static void collectAssignmentRhsKinds(EObject stmt, List<String> out) {
        if (stmt == null) {
            return;
        }
        String kind = stmt.eClass().getName();
        if ("SeqStatement".equals(kind)) {
            for (EObject s : getChildren(stmt, "statements")) {
                collectAssignmentRhsKinds(s, out);
            }
            return;
        }
        if ("Assignment".equals(kind)) {
            EObject rhs = (EObject) get(stmt, "right");
            if (rhs != null) {
                out.add(rhs.eClass().getName());
            }
        }
    }

    /** Every CallExp name in an expression tree, via reflective containment. */
    private static void collectCallExpNames(EObject expr, List<String> out) {
        if (expr == null) {
            return;
        }
        if ("CallExp".equals(expr.eClass().getName())) {
            EObject fn = (EObject) get(expr, "function");
            if (fn != null && "StringExp".equals(fn.eClass().getName())) {
                out.add((String) get(fn, "value"));
            }
        }
        for (EObject child : expr.eContents()) {
            collectCallExpNames(child, out);
        }
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
