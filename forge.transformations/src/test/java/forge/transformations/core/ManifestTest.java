package forge.transformations.core;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ManifestTest {

    @Test
    void loadsPhaseDefinitions() throws Exception {
        Path fixture = Path.of("src/test/resources/test-manifest.yaml");
        Manifest manifest = Manifest.load(fixture);

        PhaseDefinition hello = manifest.phase("hello");
        assertEquals("Hello phase", hello.label());
        assertEquals("java", hello.runnerKind());
        assertEquals("forge.transformations.core.HelloPhase", hello.javaClass());
        assertEquals("world", hello.args().get("name"));
        assertEquals(Path.of("/tmp/out"), manifest.outputDir());
    }

    @Test
    void resolvesDependsOn() throws Exception {
        Manifest manifest = Manifest.load(Path.of("src/test/resources/test-manifest.yaml"));
        assertEquals(java.util.List.of("hello"), manifest.phase("goodbye").dependsOn());
    }

    @Test
    void rejectsUnknownPhaseId() throws Exception {
        Manifest manifest = Manifest.load(Path.of("src/test/resources/test-manifest.yaml"));
        assertThrows(IllegalArgumentException.class, () -> manifest.phase("nope"));
    }
}
