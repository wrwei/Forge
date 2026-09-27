package forge.transformations.m2t;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import forge.transformations.core.PhaseContext;
import forge.transformations.t2m.T2mPhase;

/**
 * Regression tests for three java2dafny.egl defect classes fixed in the
 * TOSEM revision audit:
 *
 * 1. Silent drop of unconditional autonomous transitions — a mode branch
 *    whose body is a direct mode assignment with no inner if (the
 *    chem-detector NoGas -> Reading shape) produced an EMPTY
 *    transitionFrom body: the Dafny machine could never leave that mode.
 *
 * 2. Enum-literal field initialisers — `private Angle anl = Angle.Front;`
 *    silently emitted the enum's FIRST literal (anl := Left) because
 *    ValueResolver only folds literals/constants.
 *
 * 3. Constructor-body field initialisation — `this.cstc = -1;` in the
 *    constructor (the LRE shape) was missed by the declaration-only
 *    initValue capture, silently emitting the type default (cstc := 0).
 */
class DafnyGenerationRegressionTest {

    private Path generate(Path tmp, String controllerSource) throws Exception {
        Path src = tmp.resolve("src");
        Files.createDirectories(src.resolve("sm"));

        Files.writeString(src.resolve("sm/MyMode.java"), """
                package sm;
                public enum MyMode { S1, S2, S3 }
                """);
        Files.writeString(src.resolve("sm/Angle.java"), """
                package sm;
                public enum Angle { Left, Right, Front }
                """);
        Files.writeString(src.resolve("sm/MyEvent.java"), """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                    record GoS3() implements MyEvent {}
                }
                """);
        Files.writeString(src.resolve("sm/MyController.java"), controllerSource);

        Path output = tmp.resolve("out");
        Files.createDirectories(output);

        Map<String, Object> args = new HashMap<>();
        args.put("source", src.toString());
        args.put("output", output.toString());
        PhaseContext ctx = new PhaseContext(output, args);

        new T2mPhase().run(ctx);
        new DafnyPhase().run(ctx);

        Path dfy = output.resolve("MyController.dfy");
        assertTrue(Files.exists(dfy), "MyController.dfy should exist");
        return dfy;
    }

    @Test
    void emitsUnconditionalAutonomousTransition(@TempDir Path tmp) throws Exception {
        // S3's only behaviour is a direct mode assignment (no inner if):
        // previously extractTransitions returned an empty sequence and
        // transitionFromS3 had an empty body.
        String dafny = Files.readString(generate(tmp, """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2) {
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            if (event instanceof MyEvent.GoS3) {
                                currentMode = MyMode.S3;
                            }
                        } else if (currentMode == MyMode.S3) {
                            currentMode = MyMode.S1;
                        }
                    }
                }
                """));

        int idx = dafny.indexOf("method transitionFromS3");
        assertTrue(idx >= 0, "transitionFromS3 should exist");
        String s3Method = dafny.substring(idx, dafny.indexOf("method ", idx + 10) > 0
                ? dafny.indexOf("method ", idx + 10) : dafny.length());
        assertTrue(s3Method.contains("mode := S1;"),
                "unconditional autonomous transition S3 -> S1 must be emitted, got:\n" + s3Method);
        assertTrue(s3Method.contains("ensures mode == S1"),
                "sole unconditional transition should carry an unconditional postcondition:\n" + s3Method);
    }

    @Test
    void emitsEnumLiteralFieldInitialiser(@TempDir Path tmp) throws Exception {
        String dafny = Files.readString(generate(tmp, """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    private Angle anl = Angle.Front;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2) {
                                this.anl = Angle.Right;
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                }
                """));

        assertTrue(dafny.contains("anl := Front;"),
                "constructor must emit the declared enum literal (Front), not the first literal:\n"
                + dafny);
        assertFalse(dafny.contains("anl := Left;"),
                "the enum's first literal must NOT be silently substituted:\n" + dafny);
    }

    @Test
    void emitsConstructorBodyInitialisers(@TempDir Path tmp) throws Exception {
        String dafny = Files.readString(generate(tmp, """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    private int cstc;
                    private double tcpa;
                    public MyController() {
                        this.cstc = -1;
                        this.tcpa = -1.0;
                    }
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2 && cstc > 0) {
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                }
                """));

        assertTrue(dafny.contains("cstc := -1;"),
                "constructor-body int initialiser must be captured (was: type default 0):\n" + dafny);
        assertTrue(dafny.contains("tcpa := -1.0;"),
                "constructor-body real initialiser must be captured (was: type default 0.0):\n" + dafny);
    }
}
