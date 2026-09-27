package forge.transformations.m2t;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

import forge.transformations.core.PhaseContext;
import forge.transformations.m2m.TransformPhase;
import forge.transformations.t2m.T2mPhase;

/**
 * Regression test for the C2 extraction gap (TOSEM revision): transition
 * trigger events were written only into the trace annotation and the
 * post-state trigger set — no precondition ever read them, so
 * the zmachine's unguarded choice let an event-triggered operation fire
 * under ANY event (Tier-B witness: OCMToMOM fired without ReqMOM).
 *
 * The fix (thy_generation_rule.egl) conjoins {@code <evt> \<in> offered}
 * into each event-triggered zoperation's pre, and pins {@code offered}/{@code listens}
 * per state in the zstore invariant so the conjunct is dischargeable.
 */
class IsabelleTriggerEncodingRegressionTest {

    @Test
    void eventTriggeredOperationsGuardOnTrigger(@TempDir Path tmp) throws Exception {
        Path src = tmp.resolve("src");
        Files.createDirectories(src.resolve("sm"));

        Files.writeString(src.resolve("sm/MyMode.java"), """
                package sm;
                public enum MyMode { S1, S2, S3 }
                """);
        Files.writeString(src.resolve("sm/MyEvent.java"), """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                    record GoS3() implements MyEvent {}
                }
                """);
        Files.writeString(src.resolve("sm/MyController.java"), """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1 && event instanceof MyEvent.GoS2) {
                            currentMode = MyMode.S2;
                        } else if (currentMode == MyMode.S2 && event instanceof MyEvent.GoS3) {
                            currentMode = MyMode.S3;
                        } else if (currentMode == MyMode.S3) {
                            currentMode = MyMode.S1;
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

        // 1. Every event-triggered operation's pre must read the trigger.
        //    S1 --goS2--> S2 must be enabled only when goS2 is awaited.
        Pattern p = Pattern.compile(
                "zoperation S1ToS2 =.*?pre \"(.*?)\"", Pattern.DOTALL);
        Matcher m = p.matcher(theory);
        assertTrue(m.find(), "S1ToS2 zoperation with a pre should exist:\n" + theory);
        String pre = m.group(1);
        assertTrue(pre.contains("\\<in> offered"),
                "C2: event-triggered operation's pre must conjoin the trigger "
                + "membership, got pre \"" + pre + "\"");

        // 2. The zstore invariant must pin the STATIC `listens` lens per state so the
        //    conjunct is dischargeable in _inv / deadlock_free proofs.
        assertTrue(theory.contains("\\<longrightarrow> listens ="),
                "zstore invariant must pin the listens set per state:\n" + theory);

        // 3. Autonomous (triggerless) operations stay unconstrained:
        //    S3 -> S1 has no trigger and no same-source pre-empting branch,
        //    so its pre must not mention either event lens.
        Pattern p3 = Pattern.compile(
                "zoperation S3ToS1 =.*?pre \"(.*?)\"", Pattern.DOTALL);
        Matcher m3 = p3.matcher(theory);
        assertTrue(m3.find(), "S3ToS1 zoperation should exist");
        assertTrue(!m3.group(1).contains("offered") && !m3.group(1).contains("listens"),
                "autonomous operation must remain unconstrained, got pre \""
                + m3.group(1) + "\"");
    }
}
