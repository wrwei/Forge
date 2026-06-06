package forge.transformations.preflight;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

import forge.transformations.core.PhaseContext;

class LintPhaseTest {

    @Test
    void emitsLintReport(@TempDir Path tmp) throws Exception {
        Path src = tmp.resolve("src");
        Files.createDirectories(src);
        Files.writeString(src.resolve("Empty.java"),
                "package x; public final class Empty {}\n");
        Path output = tmp.resolve("out");
        Files.createDirectories(output);

        var ctx = new PhaseContext(output, Map.of(
                "source", src.toString(),
                "output", output.toString()));
        new LintPhase().run(ctx);

        assertTrue(Files.exists(output.resolve("lint_report.json")));
    }
}
