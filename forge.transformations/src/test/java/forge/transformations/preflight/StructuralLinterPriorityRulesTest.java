package forge.transformations.preflight;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for rule6 (priority negations in step() if/else-if chains) and
 * rule7 (signed sentinels on controller state). Both are WARNING-level
 * advisory rules; these tests pin (a) that the obligation is reported,
 * (b) that conforming sources are NOT flagged, and (c) that severity
 * stays "warning" so preflight does not fail on them.
 */
class StructuralLinterPriorityRulesTest {

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
        return violations.stream()
                .filter(v -> rule.equals(v.get("rule")))
                .count();
    }

    // ── rule6 ───────────────────────────────────────────────────────────

    @Test
    void rule6FlagsBranchMissingPriorityNegation(@TempDir Path tmp) throws Exception {
        // hcmActive branch relies on implicit else-if priority: no !camActive.
        var violations = lintSource(tmp, """
                package x;
                public class Controller {
                    private boolean camActive;
                    private boolean hcmActive;
                    private int mode;
                    public void step(Object event) {
                        if (camActive) {
                            mode = 1;
                        } else if (hcmActive) {
                            mode = 2;
                        }
                    }
                }
                """);
        assertEquals(1, count(violations, "rule6_missing_priority_negation"),
                () -> "expected one rule6 hit, got: " + violations);
        Map<String, Object> v = violations.stream()
                .filter(x -> "rule6_missing_priority_negation".equals(x.get("rule")))
                .findFirst().orElseThrow();
        assertEquals("warning", v.get("severity"));
        assertTrue(String.valueOf(v.get("message")).contains("camActive"));
    }

    @Test
    void rule6AcceptsExplicitNegations(@TempDir Path tmp) throws Exception {
        // The hand-written LRE convention, including this. qualification
        // on one side only (this.camActive vs camActive must compare equal).
        var violations = lintSource(tmp, """
                package x;
                public class Controller {
                    private boolean camActive;
                    private boolean hcmActive;
                    private boolean inOpez;
                    private int mode;
                    public void step(Object event) {
                        if (this.camActive) {
                            mode = 1;
                        } else if (hcmActive && !camActive) {
                            mode = 2;
                        } else if (inOpez && !this.camActive && !hcmActive) {
                            mode = 3;
                        }
                    }
                }
                """);
        assertEquals(0, count(violations, "rule6_missing_priority_negation"),
                () -> "conforming chain must not be flagged: " + violations);
    }

    @Test
    void rule6FlagsEventTriggeredBranchBelowTriggerlessGuard(@TempDir Path tmp)
            throws Exception {
        // Conformance gap C1 witness shape: an event-triggered branch below
        // a triggerless autonomous guard carries no negation of it.
        var violations = lintSource(tmp, """
                package x;
                public class Controller {
                    private boolean camActive;
                    private int mode;
                    public void step(Object event) {
                        if (camActive) {
                            mode = 1;
                        } else if (event instanceof String) {
                            mode = 0;
                        }
                    }
                }
                """);
        assertEquals(1, count(violations, "rule6_missing_priority_negation"));
    }

    @Test
    void rule6IgnoresModeDispatchChains(@TempDir Path tmp) throws Exception {
        // The outer currentMode == X chain is mutually exclusive by
        // construction and must not accumulate negation obligations.
        var violations = lintSource(tmp, """
                package x;
                public class Controller {
                    private int currentMode;
                    private int out;
                    public void step(Object event) {
                        if (currentMode == 1) {
                            out = 1;
                        } else if (currentMode == 2) {
                            out = 2;
                        }
                    }
                }
                """);
        assertEquals(0, count(violations, "rule6_missing_priority_negation"),
                () -> "mode-dispatch chain must not be flagged: " + violations);
    }

    @Test
    void rule6IgnoresChainsOfEventTriggeredBranches(@TempDir Path tmp) throws Exception {
        // Pure event dispatch (OCM-style): no triggerless guard, no obligation.
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
        assertEquals(0, count(violations, "rule6_missing_priority_negation"));
    }

    // ── rule7 ───────────────────────────────────────────────────────────

    @Test
    void rule7FlagsNegativeSentinelInitialisersAndComparisons(@TempDir Path tmp)
            throws Exception {
        var violations = lintSource(tmp, """
                package x;
                public class Controller {
                    private int cstc = -1;
                    private int cdyn;
                    private int mode;
                    public Controller() {
                        this.cdyn = -1;
                    }
                    public void step(Object event) {
                        if (this.cstc == -1) {
                            mode = 0;
                        } else if (cdyn >= -1 && !(this.cstc == -1)) {
                            mode = 1;
                        }
                    }
                }
                """);
        long hits = count(violations, "rule7_signed_sentinel_on_controller_state");
        // declaration init (cstc), constructor assignment (cdyn),
        // and three comparisons: cstc == -1, cdyn >= -1, cstc == -1.
        assertEquals(5, hits, () -> "rule7 hits: " + violations);
        assertTrue(violations.stream()
                .filter(v -> "rule7_signed_sentinel_on_controller_state".equals(v.get("rule")))
                .allMatch(v -> "warning".equals(v.get("severity"))));
    }

    @Test
    void rule7IgnoresNonNegativeAndNonStateUsage(@TempDir Path tmp) throws Exception {
        var violations = lintSource(tmp, """
                package x;
                public class Controller {
                    private int cstc = 0;
                    private double cda = -1.0; // double, not integer state
                    private int mode;
                    public void step(Object event) {
                        int local = -1; // local, not controller state
                        if (cstc == 0) {
                            mode = local;
                        }
                    }
                }
                """);
        assertEquals(0, count(violations, "rule7_signed_sentinel_on_controller_state"),
                () -> "no rule7 hits expected: " + violations);
    }

    // ── false-pass regression guards (Step 2 audit) ─────────────────────

    @Test
    void rule0FlagsEmptySourceRootAsError(@TempDir Path tmp) throws Exception {
        // A wrong source path used to produce "0 violations" — a vacuous
        // clean pass. It must now be an error.
        Path src = tmp.resolve("does-not-contain-java");
        Files.createDirectories(src);
        StructuralLinter linter = new StructuralLinter();
        linter.lint(src);
        assertEquals(1, count(linter.getViolations(), "rule0_no_types_found"));
        assertEquals("error", linter.getViolations().get(0).get("severity"));
    }

    @Test
    void rule2MatchesAlternateModeFieldNames(@TempDir Path tmp) throws Exception {
        // The ETL naming table recognises `mode` and `status` as the mode
        // field; rule2 used to match only `currentMode`.
        var violations = lintSource(tmp, """
                package x;
                public class Controller {
                    private int mode;
                    private int out;
                    public void step(Object event) {
                        if (mode != 1) {
                            out = 1;
                        }
                    }
                }
                """);
        assertEquals(1, count(violations, "rule2_step_outer_ne"),
                () -> "rule2 must match 'mode != X': " + violations);
    }

    @Test
    void rule7SilentWithoutStepMethod(@TempDir Path tmp) throws Exception {
        // Sentinel scan is scoped to classes containing step().
        var violations = lintSource(tmp, """
                package x;
                public class Controller {
                    private int cstc = -1;
                    public int cstc() { return cstc; }
                }
                """);
        assertEquals(0, count(violations, "rule7_signed_sentinel_on_controller_state"));
    }
}
