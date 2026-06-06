package forge.transformations.t2m;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

import forge.transformations.core.PhaseContext;

class T2mPhaseTest {

    @Test
    void discoversModelAndStoresInContext(@TempDir Path tmp) throws Exception {
        Path src = tmp.resolve("src");
        Files.createDirectories(src);
        Files.writeString(src.resolve("Empty.java"),
                "package x;\npublic final class Empty {}\n");
        Path output = tmp.resolve("out");

        var ctx = new PhaseContext(output, Map.of(
                "source", src.toString(),
                "output", output.toString()));
        new T2mPhase().run(ctx);

        assertTrue(Files.exists(output.resolve("discovered_model.xmi")));
        assertTrue(Files.exists(output.resolve("trace_t2m.json")));
        assertTrue(ctx.has("java_resource"));
        assertTrue(ctx.has("resolved_values"));
    }
}
