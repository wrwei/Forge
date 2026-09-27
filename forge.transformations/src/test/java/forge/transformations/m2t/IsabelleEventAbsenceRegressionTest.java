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
 * Regression test for the extraction gap (TOSEM revision): Java renders a
 * state's outgoing transitions as one if / else-if chain in SOURCE ORDER, so an
 * event-triggered branch appearing BEFORE an autonomous (triggerless) branch
 * pre-empts it — the autonomous branch runs only when that event is ABSENT.
 *
 * <p>The RoboChart metamodel has no expression form for event absence, so the
 * M2M stage cannot record the else-arm obligation and both transitions reach the
 * theory generator with independent guards. Witness (LRE): {@code CAMToOCM}
 * (trigger {@code reqOCM}) and {@code CAMToOCM_1} (condition
 * {@code cda >= minsafedist()}) were simultaneously satisfiable, admitting two
 * operations in 128 of 8192 abstract states where the Java is deterministic.
 *
 * <p>The fix ({@code thy_generation_rule.egl} SUBSECTION 3b) conjoins
 * {@code \<not>(<evt> \<in> offered)} into a TRIGGERLESS transition's pre for
 * every event triggering a higher-priority transition from the SAME source
 * state. This test pins the three properties that can each silently invalidate
 * it: the obligation itself, the direction restriction, and the same-source
 * restriction.
 *
 * <p>Pre-fix status: assertion 1 fails ({@code S1ToS3}'s pre carries no absence
 * conjunct). Post-fix: all four assertions pass.
 */
class IsabelleEventAbsenceRegressionTest {

    /** Extract the {@code pre "..."} string of a named zoperation. */
    private static String preOf(String theory, String zop) {
        Matcher m = Pattern.compile(
                "zoperation " + zop + " =.*?pre \"(.*?)\"", Pattern.DOTALL).matcher(theory);
        assertTrue(m.find(), "zoperation " + zop + " with a pre should exist:\n" + theory);
        return m.group(1);
    }

    @Test
    void triggerlessBranchRequiresAbsenceOfPreEmptingEvent(@TempDir Path tmp) throws Exception {
        Path src = tmp.resolve("src");
        Files.createDirectories(src.resolve("sm"));

        Files.writeString(src.resolve("sm/MyMode.java"), """
                package sm;
                public enum MyMode { S1, S2, S3, S4 }
                """);
        Files.writeString(src.resolve("sm/MyEvent.java"), """
                package sm;
                public sealed interface MyEvent {
                    record Go() implements MyEvent {}
                    record Other() implements MyEvent {}
                }
                """);
        // S1: event-triggered branch FIRST, autonomous branch SECOND
        //     -> the autonomous S1->S3 must require absence of `go`.
        // S2: autonomous branch FIRST, event-triggered branch SECOND
        //     -> nothing pre-empts the autonomous S2->S3; no absence conjunct,
        //        and the event-triggered S2->S4 must not receive one either
        //        (DIRECTION: only triggerless transitions receive them).
        Files.writeString(src.resolve("sm/MyController.java"), """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    private boolean safe = false;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.Go) {
                                currentMode = MyMode.S2;
                            } else if (safe) {
                                currentMode = MyMode.S3;
                            }
                        } else if (currentMode == MyMode.S2) {
                            if (safe) {
                                currentMode = MyMode.S3;
                            } else if (event instanceof MyEvent.Other) {
                                currentMode = MyMode.S4;
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
        String theory = Files.readString(thy);

        // 1. OBLIGATION. The autonomous S1->S3 sits AFTER the `go`-triggered
        //    S1->S2 in the Java chain, so it must require `go` to be absent.
        //    This is the assertion that fails without the fix.
        String s1ToS3 = preOf(theory, "S1ToS3");
        assertTrue(s1ToS3.contains("\\<not>(go \\<in> offered)"),
                "a triggerless transition must conjoin absence of each event "
                + "triggering a higher-priority transition from the same source; "
                + "got pre \"" + s1ToS3 + "\"");

        // 2. DIRECTION. An event-triggered operation must NOT receive absence
        //    conjuncts — at most one event is offered per step, so trigger
        //    disjointness already separates two event-triggered branches, and
        //    adding absence there can make the operation unsatisfiable.
        String s1ToS2 = preOf(theory, "S1ToS2");
        assertFalse(s1ToS2.contains("\\<not>("),
                "DIRECTION: event-triggered operations must not receive absence "
                + "conjuncts; got pre \"" + s1ToS2 + "\"");
        String s2ToS4 = preOf(theory, "S2ToS4");
        assertFalse(s2ToS4.contains("\\<not>(other \\<in> offered)")
                    || s2ToS4.contains("\\<not>(go \\<in> offered)"),
                "DIRECTION: event-triggered operations must not receive absence "
                + "conjuncts; got pre \"" + s2ToS4 + "\"");

        // 3. PRIORITY + SAME SOURCE. S2's autonomous branch comes FIRST in its
        //    chain, so nothing pre-empts it. In particular S1's `go` — a
        //    higher-priority trigger from a DIFFERENT source state — imposes no
        //    obligation, and S2's own lower-priority `other` imposes none either.
        String s2ToS3 = preOf(theory, "S2ToS3");
        assertFalse(s2ToS3.contains("\\<not>(go \\<in> offered)"),
                "SAME SOURCE: a higher-priority trigger from a different source "
                + "state must impose no absence obligation; got pre \"" + s2ToS3 + "\"");
        assertFalse(s2ToS3.contains("\\<not>(other \\<in> offered)"),
                "PRIORITY: a LOWER-priority trigger from the same source must "
                + "impose no absence obligation; got pre \"" + s2ToS3 + "\"");
    }
}
