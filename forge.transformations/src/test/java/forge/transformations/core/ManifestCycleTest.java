package forge.transformations.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ManifestCycleTest {
    @Test
    void rejectsDependencyCycle(@TempDir Path tmp) throws Exception {
        Path f = tmp.resolve("cyclic.yaml");
        Files.writeString(f, """
            phases:
              a:
                label: "A"
                runner: {kind: java, class: X}
                depends_on: [b]
              b:
                label: "B"
                runner: {kind: java, class: Y}
                depends_on: [a]
            """);
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> Manifest.load(f));
        assertTrue(ex.getMessage().contains("cycle"), ex.getMessage());
    }

    @Test
    void acceptsAcyclicManifest(@TempDir Path tmp) throws Exception {
        Path f = tmp.resolve("ok.yaml");
        Files.writeString(f, """
            phases:
              a:
                label: "A"
                runner: {kind: java, class: X}
              b:
                label: "B"
                runner: {kind: java, class: Y}
                depends_on: [a]
            """);
        assertEquals(java.util.Set.of("a", "b"), Manifest.load(f).phaseIds());
    }
}
