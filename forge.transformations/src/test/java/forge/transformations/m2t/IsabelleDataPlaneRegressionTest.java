package forge.transformations.m2t;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import forge.transformations.core.PhaseContext;
import forge.transformations.m2m.TransformPhase;
import forge.transformations.t2m.T2mPhase;

/**
 * Regression test for the I1 defect (TOSEM revision): the Isabelle DATA PLANE
 * was frozen.
 *
 * <p>Pre-fix, {@code thy_generation_rule.egl} emitted, per transition, an
 * {@code update} block assigning only {@code st}, {@code tr} and
 * {@code triggers}. Every zstore DATA lens — the controller state variables
 * defined by the operation bodies — was assigned by NO operation, so the
 * Z-Machines frame rule left each one arbitrary but CONSTANT for a whole run,
 * while the Java re-evaluates all of them at the top of every {@code step()}.
 * The theory therefore UNDER-approximates the Java: it excludes behaviour the
 * implementation exhibits, which is unsound for liveness. (Measured on the
 * case studies: LRE 8 frozen data variables, chemical detector 4, SRanger 1.)
 *
 * <p>The fix collects each operation body's assignments and emits them into
 * every operation's update block. The subtle part is that a Z-Machines
 * {@code update} is a SIMULTANEOUS substitution, so a naive emission
 * {@code [tcpa\<Zprime> = E, cda\<Zprime> = f(tcpa)]} makes {@code cda} read
 * the STALE {@code tcpa} — reintroducing at the Isabelle stage the stale-read
 * defect (U3) fixed at M2M. The emitter therefore INLINES dependencies:
 * {@code cda\<Zprime> = f(E)}.
 *
 * <p>Three properties are pinned here, each of which can silently regress:
 * <ol>
 *   <li>COVERAGE — every data variable moves in at least one operation
 *       (fails pre-fix);</li>
 *   <li>INLINING — a dependent variable's emitted expression contains its
 *       dependency's expression, not the bare dependency name (fails pre-fix
 *       vacuously, and would fail again under a naive non-inlining emitter
 *       that restored coverage only);</li>
 *   <li>CYCLES — a genuine fixpoint is reported as an {@code I1-CYCLE} marker
 *       rather than inlined into something plausible, and does not diverge.</li>
 * </ol>
 */
class IsabelleDataPlaneRegressionTest {

    /** Extract the {@code update "[...]"} body of a named zoperation. */
    private static String updateOf(String theory, String zop) {
        Matcher m = Pattern.compile(
                "zoperation " + zop + " =.*?update \"\\[(.*?)\\]\"", Pattern.DOTALL).matcher(theory);
        assertTrue(m.find(), "zoperation " + zop + " with an update should exist:\n" + theory);
        return m.group(1);
    }

    private static Path generate(Path tmp, String controller, String... extraFiles) throws Exception {
        Path src = tmp.resolve("src");
        Files.createDirectories(src.resolve("sm"));
        Files.writeString(src.resolve("sm/MyMode.java"), """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        Files.writeString(src.resolve("sm/MyEvent.java"), """
                package sm;
                public sealed interface MyEvent {
                    record Go() implements MyEvent {}
                }
                """);
        Files.writeString(src.resolve("sm/Sensor.java"), """
                package sm;
                public final class Sensor {
                    public double raw() { return 0.0; }
                }
                """);
        for (int i = 0; i + 1 < extraFiles.length; i += 2) {
            Files.writeString(src.resolve(extraFiles[i]), extraFiles[i + 1]);
        }
        Files.writeString(src.resolve("sm/MyController.java"), controller);

        Path output = tmp.resolve("out");
        Files.createDirectories(output);
        Map<String, Object> args = new HashMap<>();
        args.put("source", src.toString());
        args.put("output", output.toString());
        PhaseContext ctx = new PhaseContext(output, args);
        new T2mPhase().run(ctx);
        new TransformPhase().run(ctx);
        new IsabellePhase().run(ctx);
        return output.resolve("isabelle/MyController_Beh.thy");
    }

    /**
     * COVERAGE + INLINING. {@code Calc.compute()} assigns {@code base} then
     * {@code derived}, where {@code derived} READS {@code base} — the shape of
     * LRE's {@code CalcCPA} ({@code tcpa} then {@code cda}). Both are lifted to
     * zstore lenses because the controller's guard reads them.
     */
    @Test
    void dataVariablesAreUpdatedWithDependenciesInlined(@TempDir Path tmp) throws Exception {
        Path thy = generate(tmp, """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    private final Sensor sensor;
                    private final Calc calc;
                    private double base;
                    private double derived;
                    public MyController(Sensor sensor, Calc calc) {
                        this.sensor = sensor;
                        this.calc = calc;
                        this.base = 0.0;
                        this.derived = 0.0;
                    }
                    public void step(MyEvent event) {
                        calc.compute();
                        this.base = calc.base();
                        this.derived = calc.derived();
                        boolean derivedHigh = derived >= 1.0;
                        boolean baseHigh = base >= 1.0;
                        if (currentMode == MyMode.S1) {
                            if (derivedHigh) {
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            if (baseHigh) {
                                currentMode = MyMode.S1;
                            }
                            // Present only so the model lifts a non-empty event
                            // set; the template's `enumtype Evt` emission
                            // requires one. Not otherwise load-bearing here.
                            else if (event instanceof MyEvent.Go) {
                                currentMode = MyMode.S1;
                            }
                        }
                    }
                }
                """,
                "sm/Calc.java", """
                package sm;
                public final class Calc {
                    private final Sensor sensor;
                    private double base;
                    private double derived;
                    public Calc(Sensor sensor) {
                        this.sensor = sensor;
                        this.base = 0.0;
                        this.derived = 0.0;
                    }
                    public void compute() {
                        this.base = sensor.raw() * 2.0;
                        this.derived = this.base + 1.0;
                    }
                    public double base() { return base; }
                    public double derived() { return derived; }
                }
                """);
        assertTrue(Files.exists(thy), "theory file should exist");
        String theory = Files.readString(thy);

        // 1. COVERAGE. Both data variables must be declared as zstore lenses
        //    AND assigned in at least one operation. Pre-fix, NEITHER
        //    `\<Zprime>` form appears anywhere in the theory.
        assertTrue(theory.contains("base :: \"real\""),
                "precondition of this test: `base` should be a zstore lens:\n" + theory);
        assertTrue(theory.contains("derived :: \"real\""),
                "precondition of this test: `derived` should be a zstore lens:\n" + theory);
        assertTrue(theory.contains("base\\<Zprime>"),
                "I1 COVERAGE: the data variable `base` is recomputed every Java step(), "
                + "so it must be assigned in the update block of at least one operation; "
                + "pre-fix it was assigned by none and so was frozen for a whole run:\n" + theory);
        assertTrue(theory.contains("derived\\<Zprime>"),
                "I1 COVERAGE: the data variable `derived` must be assigned in at least one "
                + "operation's update block:\n" + theory);

        // 2. INLINING. `update` is a SIMULTANEOUS substitution, so a bare
        //    `derived\<Zprime> = (base + 1.0)` would read the PRE-state `base`
        //    — the stale value, not the one this same block computes. The
        //    emitter must splice in `base`'s own expression instead. This is
        //    the assertion that distinguishes the real fix from a naive
        //    coverage-only emitter.
        String upd = updateOf(theory, "S1ToS2");
        Matcher dm = Pattern.compile("derived\\\\<Zprime> = ([^\\n]*)").matcher(upd);
        assertTrue(dm.find(), "S1ToS2 should assign derived:\n" + upd);
        String derivedRhs = dm.group(1);
        assertTrue(derivedRhs.contains("raw()"),
                "I1 INLINING: `derived`'s emitted expression must contain the INLINED "
                + "expression of its dependency `base` (which is `raw() * 2.0`), because a "
                + "simultaneous substitution would otherwise read the stale pre-state `base`; "
                + "got \"" + derivedRhs + "\"");
        assertFalse(derivedRhs.matches(".*(^|[^A-Za-z0-9_])base([^A-Za-z0-9_(]|$).*"),
                "I1 INLINING: `derived`'s expression must NOT read the bare lens `base` — "
                + "that is the pre-state (stale) value under simultaneous substitution; "
                + "got \"" + derivedRhs + "\"");
    }

    /**
     * CYCLES. A genuine fixpoint ({@code x = g(y); y = h(x)}) has no finite
     * inlined form. The emitter must terminate and mark the affected variable
     * rather than silently substituting a pre-state value.
     */
    @Test
    void cyclicDataDependencyIsMarkedNotSilentlyInlined(@TempDir Path tmp) throws Exception {
        Path thy = generate(tmp, """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    private final Loop loop;
                    private double x;
                    private double y;
                    public MyController(Loop loop) {
                        this.loop = loop;
                        this.x = 0.0;
                        this.y = 0.0;
                    }
                    public void step(MyEvent event) {
                        loop.compute();
                        this.x = loop.x();
                        this.y = loop.y();
                        boolean xHigh = x >= 1.0;
                        boolean yHigh = y >= 1.0;
                        if (currentMode == MyMode.S1) {
                            if (xHigh) {
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            if (yHigh) {
                                currentMode = MyMode.S1;
                            }
                            // See the twin test: needed only so a non-empty
                            // event set is lifted.
                            else if (event instanceof MyEvent.Go) {
                                currentMode = MyMode.S1;
                            }
                        }
                    }
                }
                """,
                "sm/Loop.java", """
                package sm;
                public final class Loop {
                    private double x;
                    private double y;
                    public Loop() {
                        this.x = 0.0;
                        this.y = 0.0;
                    }
                    public void compute() {
                        this.x = this.y + 1.0;
                        this.y = this.x + 1.0;
                    }
                    public double x() { return x; }
                    public double y() { return y; }
                }
                """);
        assertTrue(Files.exists(thy), "theory file should exist");
        String theory = Files.readString(thy);

        // Termination is itself the primary assertion: an unguarded inliner
        // recurses forever on this input and the test never returns. Given it
        // returned, the cycle must be REPORTED rather than papered over.
        assertTrue(theory.contains("I1-CYCLE"),
                "I1 CYCLES: a genuine fixpoint (x = y + 1; y = x + 1) is not soundly "
                + "inlineable, so the emitter must leave an `I1-CYCLE` marker naming the "
                + "affected variable instead of emitting a plausible-looking expression:\n"
                + theory);
    }
}
