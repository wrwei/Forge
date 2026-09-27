package forge.transformations.m2t;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import forge.transformations.core.PhaseContext;
import forge.transformations.t2m.T2mPhase;

class DafnyPhaseTest {

    @Test
    void emitsDafnyFileAndTrace(@TempDir Path tmp) throws Exception {
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

        // Populate ctx with discovery results (same as pipeline ordering)
        new T2mPhase().run(ctx);

        new DafnyPhase().run(ctx);

        assertTrue(Files.exists(output.resolve("MyController.dfy")),
                "MyController.dfy should exist");
        assertTrue(Files.exists(output.resolve("trace_dafny.json")),
                "trace_dafny.json should exist");
    }
}
