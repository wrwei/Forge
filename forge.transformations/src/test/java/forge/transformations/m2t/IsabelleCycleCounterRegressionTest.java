package forge.transformations.m2t;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import forge.transformations.core.PhaseContext;
import forge.transformations.m2m.TransformPhase;
import forge.transformations.t2m.T2mPhase;

/**
 * Regression test for the SRanger cycle-counter shape (I1 close, TOSEM
 * revision): a controller that carries its timing as OBSERVABLE STATE —
 * a per-step counter, a branch-written start marker, and a top-level
 * derived predicate:
 *
 * <pre>
 *   this.cycleCount = this.cycleCount + 1.0;                  // top of step()
 *   this.flag = this.cycleCount - this.startCycle >= LIMIT;   // top of step()
 *   ... inside a branch: this.startCycle = this.cycleCount;   // transition action
 * </pre>
 *
 * Three properties are pinned, each of which regressed at least once while
 * the emitters were being built:
 * <ol>
 *   <li>SELF-READ — {@code cycleCount' = cycleCount + 1} must read the bare
 *       PRE-state lens under simultaneous substitution (like
 *       {@code tr' = tr @ [...]}): no {@code I1-CYCLE} marker, no
 *       double-increment from the inliner substituting the definition into
 *       itself.</li>
 *   <li>ORDERING — the branch assignment {@code startCycle' = cycleCount}
 *       must record THIS step's incremented count (the Java runs the
 *       increment before the branch), so its emitted RHS is the inlined
 *       {@code (cycleCount + 1.0)}, not the stale bare lens.</li>
 *   <li>GUARD-SKEW — a precondition guarding on the derived predicate must
 *       dispatch on the value the Java computes THIS step: the inlined
 *       {@code ((cycleCount + 1.0)) - startCycle >= limit()}, not the bare
 *       {@code flag} lens (which holds the PREVIOUS step's value and admits
 *       a spurious exit one step after entry).</li>
 * </ol>
 */
class IsabelleCycleCounterRegressionTest {

    private static String theory(Path tmp) throws Exception {
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
                    record Tick() implements MyEvent {}
                }
                """);
        Files.writeString(src.resolve("sm/Sensor.java"), """
                package sm;
                public final class Sensor {
                    public double raw() { return 0.0; }
                }
                """);
        Files.writeString(src.resolve("sm/OutEvent.java"), """
                package sm;
                public sealed interface OutEvent {
                    record Move(double v) implements OutEvent {}
                }
                """);
        Files.writeString(src.resolve("sm/Actuator.java"), """
                package sm;
                public final class Actuator {
                    public void apply(OutEvent e) { }
                }
                """);
        Files.writeString(src.resolve("sm/MyConstants.java"), """
                package sm;
                public final class MyConstants {
                    public static final double limit = 20.0;
                    public static final double other = 1.0;
                    private MyConstants() {}
                }
                """);
        Files.writeString(src.resolve("sm/MyController.java"), """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    private final Sensor sensor;
                    private final Actuator actuator;
                    private double cycleCount;
                    private double startCycle;
                    private boolean flag;
                    public MyController(Sensor sensor, Actuator actuator) {
                        this.sensor = sensor;
                        this.actuator = actuator;
                        this.cycleCount = 0.0;
                        this.startCycle = 0.0;
                        this.flag = false;
                    }
                    public void step(MyEvent event) {
                        this.cycleCount = this.cycleCount + 1.0;
                        boolean high = sensor.raw() >= MyConstants.other;
                        this.flag = this.cycleCount - this.startCycle >= MyConstants.limit;
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.Go && high) {
                                currentMode = MyMode.S2;
                                // Mirror the sranger Moving -> Turning branch shape:
                                // a state-var assignment PLUS an actuator output, so
                                // the extracted action is a SeqStatement. (A branch
                                // whose ONLY statement is the assignment produces a
                                // bare Assignment action that the template's
                                // lens-collection walk misses -- a pre-existing gap
                                // recorded as a residual finding, not this test's
                                // subject.)
                                this.startCycle = this.cycleCount;
                                actuator.apply(new OutEvent.Move(0.0));
                            }
                            else if (event instanceof MyEvent.Tick) {
                                currentMode = MyMode.S1;
                            }
                        } else if (currentMode == MyMode.S2) {
                            if (flag) {
                                currentMode = MyMode.S1;
                            }
                            else if (event instanceof MyEvent.Tick) {
                                currentMode = MyMode.S2;
                            }
                        }
                    }
                }
                """);
        Path output = tmp.resolve("out");
        Files.createDirectories(output);
        Map<String, Object> args = new HashMap<>();
        args.put("source", src.toString());
        args.put("output", output.toString());
        PhaseContext ctx = new PhaseContext(output, args);
        new T2mPhase().run(ctx);
        new TransformPhase().run(ctx);
        new IsabellePhase().run(ctx);
        Path thy = output.resolve("isabelle/MyController_Beh.thy");
        assertTrue(Files.exists(thy), "theory file should exist");
        return Files.readString(thy);
    }

    @Test
    void cycleCounterSelfReadOrderingAndGuardSkew(@TempDir Path tmp) throws Exception {
        String thy = theory(tmp);

        // 1. SELF-READ: every cycleCount clause is exactly `(cycleCount + 1.0)`
        //    — bare pre-state lens, single increment, no cycle marker.
        Matcher cc = Pattern.compile("cycleCount\\\\<Zprime> = ([^\\n]*)").matcher(thy);
        int n = 0;
        while (cc.find()) {
            n++;
            assertEquals("(cycleCount + 1.0)", cc.group(1).trim(),
                    "SELF-READ: cycleCount' must read the bare pre-state lens once");
        }
        assertTrue(n >= 2, "cycleCount must be assigned in the update blocks (got " + n + ")");
        assertFalse(thy.contains("I1-CYCLE"),
                "SELF-READ: a self-reading counter is NOT a fixpoint and must not be I1-CYCLE-marked:\n" + thy);

        // 2. ORDERING: the branch-written startCycle records THIS step's count.
        Matcher sc = Pattern.compile("startCycle\\\\<Zprime> = ([^\\n]*)").matcher(thy);
        assertTrue(sc.find(), "S1ToS2 must assign startCycle:\n" + thy);
        assertTrue(sc.group(1).contains("cycleCount + 1.0"),
                "ORDERING: startCycle' must record the INCREMENTED count (the Java "
                + "increments before the branch); got \"" + sc.group(1).trim() + "\"");

        // 3. GUARD-SKEW: the S2 -> S1 precondition dispatches on the inlined
        //    THIS-step predicate, not the stale `flag` lens.
        Matcher pre = Pattern.compile("pre \"st= S2 \\\\<and> ([^\"]*)\"").matcher(thy);
        boolean sawInlined = false;
        while (pre.find()) {
            String cond = pre.group(1);
            if (cond.contains("cycleCount + 1.0") && cond.contains("startCycle")) {
                sawInlined = true;
                assertFalse(cond.matches(".*(^|[^A-Za-z0-9_])flag([^A-Za-z0-9_(]|$).*"),
                        "GUARD-SKEW: the inlined guard must not ALSO read the stale flag lens: " + cond);
            }
        }
        assertTrue(sawInlined,
                "GUARD-SKEW: some S2-source precondition must inline the dispatch-time "
                + "predicate ((cycleCount + 1.0)) - startCycle >= limit():\n" + thy);
    }
}
