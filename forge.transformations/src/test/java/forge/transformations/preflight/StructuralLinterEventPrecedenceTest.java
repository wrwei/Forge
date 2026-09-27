package forge.transformations.preflight;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression guard for sibling defect S2 of the C1 priority gap
 * ({@code review/semantic-preservation-audit-2026-08-31.md} §5).
 *
 * <p>{@code checkPriorityNegations} only ever added TRIGGERLESS guards to
 * {@code precedingCores}, so {@code rule6_missing_priority_negation} could not
 * fire on an instance of this shape by construction: an event-triggered branch followed by
 * a triggerless guarded branch. The LRE CAM block is exactly that shape and
 * produced no finding in any of the three M2M runs — the lint written to
 * document the C1 obligation was blind to the residual the C1 fix leaves.
 *
 * <p>{@code rule8_event_branch_precedes_triggerless} makes the shape visible.
 * It is reported separately from rule6 because the remedy differs: rule6's
 * "conjoin {@code && !<guard>}" does not apply when the earlier branch
 * contributes no data predicate at all.
 */
class StructuralLinterEventPrecedenceTest {

    private static List<Map<String, Object>> lintSource(Path tmp, String source)
            throws Exception {
        Path src = tmp.resolve("src");
        Files.createDirectories(src);
        Files.writeString(src.resolve("Controller.java"), source);
        StructuralLinter linter = new StructuralLinter();
        linter.lint(src);
        return linter.getViolations();
    }

    private static long count(List<Map<String, Object>> violations, String rule) {
        return violations.stream().filter(v -> rule.equals(v.get("rule"))).count();
    }

    /**
     * The shape itself, transcribed from LreController.java's CAM block:
     * {@code if (event instanceof ReqOCM) ... else if (cdaAboveOrAtMinSafe) ...}.
     * Pre-S2 this produced ZERO findings of any rule.
     */
    @Test
    void rule8FlagsTriggerlessBranchAfterEventBranch(@TempDir Path tmp) throws Exception {
        var violations = lintSource(tmp, """
                package x;
                public class Controller {
                    private double cda;
                    private int mode;
                    public void step(Object event) {
                        boolean cdaAboveOrAtMinSafe = cda >= 1.0;
                        if (event instanceof String) {
                            mode = 1;
                        } else if (cdaAboveOrAtMinSafe) {
                            mode = 2;
                        }
                    }
                }
                """);
        assertEquals(1, count(violations, "rule8_event_branch_precedes_triggerless"),
                () -> "S2: the shape (event-triggered branch followed by a "
                      + "triggerless guarded branch) must be reported. Got: " + violations);

        Map<String, Object> v = violations.stream()
                .filter(x -> "rule8_event_branch_precedes_triggerless".equals(x.get("rule")))
                .findFirst().orElseThrow();
        assertEquals("warning", v.get("severity"),
                "rule8 is advisory like rule6 — preflight must not fail on it");
        assertTrue(String.valueOf(v.get("message")).contains("cdaAboveOrAtMinSafe"),
                "the message must name the triggerless branch that becomes co-enabled");
    }

    /**
     * Two event-triggered branches are NOT flagged: at most one event is
     * offered per step, so trigger disjointness already makes them mutually
     * exclusive. Flagging these would bury the real instances in noise.
     */
    @Test
    void rule8IgnoresTwoEventTriggeredBranches(@TempDir Path tmp) throws Exception {
        var violations = lintSource(tmp, """
                package x;
                public class Controller {
                    private int mode;
                    public void step(Object event) {
                        if (event instanceof String) {
                            mode = 1;
                        } else if (event instanceof Integer) {
                            mode = 2;
                        }
                    }
                }
                """);
        assertEquals(0, count(violations, "rule8_event_branch_precedes_triggerless"),
                () -> "trigger disjointness already separates two event-triggered "
                      + "branches — no obligation. Got: " + violations);
    }

    /**
     * The reverse order is NOT an instance of this shape: a triggerless branch followed by
     * an event-triggered one is handled by the existing priority-negation
     * synthesis (the ETL accumulates the triggerless guard and the later branch
     * receives its negation), which is what rule6 already documents.
     */
    @Test
    void rule8IgnoresTriggerlessBeforeEventBranch(@TempDir Path tmp) throws Exception {
        var violations = lintSource(tmp, """
                package x;
                public class Controller {
                    private boolean camActive;
                    private int mode;
                    public void step(Object event) {
                        if (camActive) {
                            mode = 1;
                        } else if (event instanceof String) {
                            mode = 2;
                        }
                    }
                }
                """);
        assertEquals(0, count(violations, "rule8_event_branch_precedes_triggerless"),
                () -> "triggerless-then-event is covered by negation synthesis, "
                      + "not by rule8. Got: " + violations);
    }

    /**
     * Guard against over-reporting on the common conforming shape: a chain of
     * event-triggered branches with no triggerless branch anywhere.
     */
    @Test
    void rule8SilentOnPureEventChain(@TempDir Path tmp) throws Exception {
        var violations = lintSource(tmp, """
                package x;
                public class Controller {
                    private int mode;
                    public void step(Object event) {
                        if (event instanceof String) {
                            mode = 1;
                        } else if (event instanceof Integer) {
                            mode = 2;
                        } else if (event instanceof Double) {
                            mode = 3;
                        }
                    }
                }
                """);
        assertEquals(0, count(violations, "rule8_event_branch_precedes_triggerless"),
                () -> "no triggerless branch, no obligation. Got: " + violations);
    }
}
