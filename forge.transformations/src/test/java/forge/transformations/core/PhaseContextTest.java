package forge.transformations.core;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PhaseContextTest {

    @Test
    void argPathReturnsResolvedPath() {
        var ctx = new PhaseContext(Path.of("/tmp/out"),
                Map.of("source", "/tmp/src"));
        assertEquals(Path.of("/tmp/src"), ctx.argPath("source"));
    }

    @Test
    void requireThrowsOnMissingKey() {
        var ctx = new PhaseContext(Path.of("/tmp"), Map.of());
        var ex = assertThrows(IllegalStateException.class,
                () -> ctx.require("rc_resource", Object.class));
        assertTrue(ex.getMessage().contains("rc_resource"));
    }

    @Test
    void putThenRequireRoundTrips() {
        var ctx = new PhaseContext(Path.of("/tmp"), Map.of());
        var value = "hello";
        ctx.put("greeting", value);
        assertEquals(value, ctx.require("greeting", String.class));
        assertTrue(ctx.has("greeting"));
    }
}
