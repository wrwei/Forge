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
 * java2dafny.egl renders the MAX_VALUE / MIN_VALUE constants of the numeric
 * wrapper types as values. Before this, only Double's were rendered, and
 * `static final int N = Integer.MAX_VALUE;` became `const N: int := MAX_VALUE`,
 * an identifier Dafny cannot resolve.
 */
class DafnyWrapperConstantRegressionTest {

    @Test
    void rendersWrapperConstantsAsValues(@TempDir Path tmp) throws Exception {
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
        Files.writeString(src.resolve("sm/MyController.java"), """
                package sm;
                public final class MyController {
                    static final int IMAX = Integer.MAX_VALUE;
                    static final int IMIN = Integer.MIN_VALUE;
                    static final long LMAX = Long.MAX_VALUE;
                    static final short SMAX = Short.MAX_VALUE;
                    static final byte BMIN = Byte.MIN_VALUE;
                    static final float FMAX = Float.MAX_VALUE;
                    private MyMode currentMode = MyMode.S1;
                    private int count = 0;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.Go && count < IMAX) {
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            if (event instanceof MyEvent.Go) { currentMode = MyMode.S1; }
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
        new DafnyPhase().run(ctx);
        String dafny = Files.readString(output.resolve("MyController.dfy"));

        assertTrue(dafny.contains("const IMAX: int := 2147483647"), dafny);
        assertTrue(dafny.contains("const IMIN: int := (-2147483648)"), dafny);
        assertTrue(dafny.contains("const LMAX: int := 9223372036854775807"), dafny);
        assertTrue(dafny.contains("const SMAX: int := 32767"), dafny);
        assertTrue(dafny.contains("const BMIN: int := (-128)"), dafny);
        assertTrue(dafny.contains("const FMAX: real := 340282350000000000000000000000000000000.0"), dafny);
        assertFalse(dafny.matches("(?s).*:=\\s*(MAX_VALUE|MIN_VALUE)\\b.*"),
                "no bare MAX_VALUE / MIN_VALUE may remain:\n" + dafny);
    }
}
