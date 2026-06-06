package forge.transformations.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhaseRegistryTest {

    public static final class GoodPhase implements Phase {
        @Override public void run(PhaseContext ctx) {}
    }

    public static final class NotAPhase {
        public NotAPhase() {}
    }

    public static final class NoNoArgCtorPhase implements Phase {
        public NoNoArgCtorPhase(String required) {}
        @Override public void run(PhaseContext ctx) {}
    }

    @Test
    void loadsValidPhaseClass() {
        Phase phase = PhaseRegistry.load(
            "forge.transformations.core.PhaseRegistryTest$GoodPhase");
        assertNotNull(phase);
        assertTrue(phase instanceof GoodPhase);
    }

    @Test
    void throwsWhenClassDoesNotExist() {
        var ex = assertThrows(IllegalStateException.class,
            () -> PhaseRegistry.load("does.not.Exist"));
        assertTrue(ex.getMessage().contains("Cannot load phase"));
        assertTrue(ex.getMessage().contains("does.not.Exist"));
    }

    @Test
    void throwsWhenClassDoesNotImplementPhase() {
        var ex = assertThrows(IllegalStateException.class,
            () -> PhaseRegistry.load(
                "forge.transformations.core.PhaseRegistryTest$NotAPhase"));
        assertTrue(ex.getMessage().contains("does not implement"));
    }

    @Test
    void throwsWhenClassHasNoNoArgConstructor() {
        var ex = assertThrows(IllegalStateException.class,
            () -> PhaseRegistry.load(
                "forge.transformations.core.PhaseRegistryTest$NoNoArgCtorPhase"));
        assertTrue(ex.getMessage().contains("Cannot load phase"));
    }
}
