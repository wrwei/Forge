package forge.transformations.m2t;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

import forge.transformations.core.PhaseContext;
import forge.transformations.m2m.TransformPhase;
import forge.transformations.t2m.T2mPhase;

class RctPhaseTest {

    @Test
    void emitsRctFileAndTrace(@TempDir Path tmp) throws Exception {
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

        // Run T2M + Transform to populate rc_resource in ctx
        new T2mPhase().run(ctx);
        new TransformPhase().run(ctx);

        // Now run RCT phase using in-memory rc_resource
        new RctPhase().run(ctx);

        assertTrue(Files.exists(output.resolve("robochart_controller.rct")),
                "robochart_controller.rct should exist");
        assertTrue(Files.exists(output.resolve("trace_m2t_rct.json")),
                "trace_m2t_rct.json should exist");
    }
}
