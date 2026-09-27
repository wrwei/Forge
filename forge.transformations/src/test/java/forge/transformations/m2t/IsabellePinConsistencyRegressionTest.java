package forge.transformations.m2t;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import forge.transformations.core.PhaseContext;
import forge.transformations.m2m.TransformPhase;
import forge.transformations.t2m.T2mPhase;

/**
 * Regression test for the event-absence encoding (TOSEM revision): NO emitted precondition
 * may contradict the zstore invariant's per-state pin.
 *
 * <p><b>The defect this pins.</b> The C2 fix pinned, per state {@code S},
 * {@code st = S \<longrightarrow> triggers = T_S} and ALSO read the same
 * {@code triggers} lens in trigger-presence conjuncts. The fix then emitted
 * {@code \<not>(<evt> \<in> triggers)} on triggerless transitions for each
 * pre-empting event {@code <evt>} of the same source state. Every such
 * {@code <evt>} is by construction in {@code T_S}, so the pin makes
 * {@code <evt> \<in> triggers} TRUE and the absence conjunct FALSE: the
 * precondition is unsatisfiable and the operation is PROOF-DEAD. Witness (LRE):
 * {@code CAMToOCM_1} in state {@code CAM}, pinned to {@code {reqOCM}}, carrying
 * {@code \<not>(reqOCM \<in> triggers)}.
 *
 * <p><b>Root cause.</b> One variable carrying two meanings — the events a state
 * LISTENS FOR (static, automaton-side, what the pin records) and the events
 * OFFERED this step (dynamic, environment-side, what a guard must read).
 *
 * <p><b>The fix.</b> {@code thy_generation_rule.egl} splits the lens into
 * {@code listens} (static, pinned) and {@code offered} (dynamic, unpinned free
 * input). Presence and absence conjuncts both read {@code offered}; the link
 * {@code offered \<subseteq> listens} keeps the pin load-bearing.
 *
 * <p><b>Pre-fix status.</b> {@link #noEmittedPreconditionContradictsAPin} fails
 * on assertion 1 (CAMToOCM_1-shaped operation is unsatisfiable).
 * {@link #pinsAreNotVacuous} passes pre-fix too — it is the guard against
 * "fixing" the contradiction by making the pins meaningless, which would trade
 * a contradiction for a vacuity (exactly reviewer R2's criticism of our
 * invariants) and is no better.
 */
class IsabellePinConsistencyRegressionTest {

    /** {@code (st = S \<longrightarrow> <lens> = {a, b})} in the zstore invariant. */
    private static final Pattern PIN = Pattern.compile(
            "\\(st = (\\w+) \\\\<longrightarrow> (triggers|listens) = \\{([^}]*)\\}\\)");
    /** A zoperation's name and its {@code pre "..."} string. */
    private static final Pattern ZOP = Pattern.compile(
            "zoperation (\\w+) =\\s*\\n\\s*over \\w+\\s*\\n\\s*pre \"(.*?)\"", Pattern.DOTALL);
    /** {@code <evt> \<in> <lens>}. */
    private static final Pattern MEMBER = Pattern.compile(
            "^(\\w+) \\\\<in> (triggers|offered|listens)$");
    /** {@code \<not>(<evt> \<in> <lens>)}. */
    private static final Pattern NOT_MEMBER = Pattern.compile(
            "^\\\\<not>\\((\\w+) \\\\<in> (triggers|offered|listens)\\)$");
    /** {@code <lens> \<subseteq> <lens>}. */
    private static final Pattern SUBSET = Pattern.compile(
            "^(\\w+) \\\\<subseteq> (\\w+)$");
    /** {@code st= S} — the state conjunct that selects which pin applies. */
    private static final Pattern STATE = Pattern.compile("^st\\s*=\\s*(\\w+)$");

    /** One emitted operation, reduced to its event-set fragment. */
    private record Frag(String op, String state,
                        Map<String, Set<String>> pos,   // lens -> events required present
                        Map<String, Set<String>> neg,   // lens -> events required absent
                        Set<String> subsetOf) {}        // lenses L with `L \<subseteq> M`, keyed by L->M below

    /** Parse the per-state pins out of the zstore invariant. */
    private static Map<String, Set<String>> pins(String theory) {
        Map<String, Set<String>> out = new LinkedHashMap<>();
        Matcher m = PIN.matcher(theory);
        while (m.find()) {
            Set<String> evts = new LinkedHashSet<>();
            for (String e : m.group(3).split(",")) {
                if (!e.isBlank()) evts.add(e.trim());
            }
            out.put(m.group(1), evts);
        }
        return out;
    }

    /** Which lens each pin constrains (all pins use the same one). */
    private static String pinnedLens(String theory) {
        Matcher m = PIN.matcher(theory);
        return m.find() ? m.group(2) : null;
    }

    /**
     * Reduce every zoperation to its event-set fragment. Preconditions are a
     * flat {@code \<and>}-separated conjunction in this emission; conjuncts that
     * are not event-set atoms (data guards) are ignored, because they share no
     * variable with the event-set fragment and so cannot rescue or break its
     * satisfiability.
     */
    private static List<Frag> fragments(String theory) {
        List<Frag> out = new ArrayList<>();
        Matcher m = ZOP.matcher(theory);
        while (m.find()) {
            String op = m.group(1);
            String state = null;
            Map<String, Set<String>> pos = new LinkedHashMap<>();
            Map<String, Set<String>> neg = new LinkedHashMap<>();
            Set<String> subset = new LinkedHashSet<>();
            for (String raw : m.group(2).split(Pattern.quote("\\<and>"))) {
                String c = raw.trim();
                Matcher s = STATE.matcher(c);
                if (s.matches()) { state = s.group(1); continue; }
                Matcher nm = NOT_MEMBER.matcher(c);
                if (nm.matches()) {
                    neg.computeIfAbsent(nm.group(2), k -> new LinkedHashSet<>()).add(nm.group(1));
                    continue;
                }
                Matcher mm = MEMBER.matcher(c);
                if (mm.matches()) {
                    pos.computeIfAbsent(mm.group(2), k -> new LinkedHashSet<>()).add(mm.group(1));
                    continue;
                }
                Matcher sb = SUBSET.matcher(c);
                if (sb.matches()) { subset.add(sb.group(1) + "\u2286" + sb.group(2)); }
            }
            out.add(new Frag(op, state, pos, neg, subset));
        }
        return out;
    }

    /**
     * Why {@code f}'s event-set fragment is unsatisfiable under the pins, or
     * {@code null} if a witness valuation exists.
     *
     * <p>Decision procedure. The pinned lens takes exactly the pinned value in a
     * pinned state. An unpinned lens is free, except that {@code L \<subseteq> M}
     * bounds {@code L} by {@code M}'s value. A fragment is satisfiable iff no
     * event is required both present and absent on one lens, every atom on the
     * pinned lens agrees with the pin, and every positively-required event on a
     * subset-bounded lens lies inside the bound.
     */
    private static String unsat(Frag f, Map<String, Set<String>> pins, String pinnedLens) {
        Set<String> pin = f.state() == null ? null : pins.get(f.state());
        for (String lens : new LinkedHashSet<>(concat(f.pos().keySet(), f.neg().keySet()))) {
            Set<String> p = f.pos().getOrDefault(lens, Set.of());
            Set<String> n = f.neg().getOrDefault(lens, Set.of());
            Set<String> both = new LinkedHashSet<>(p);
            both.retainAll(n);
            if (!both.isEmpty()) {
                return f.op() + ": " + both + " required both present and absent in `" + lens + "`";
            }
            if (pin != null && lens.equals(pinnedLens)) {
                Set<String> outside = new LinkedHashSet<>(p);
                outside.removeAll(pin);
                if (!outside.isEmpty()) {
                    return f.op() + ": pin `" + f.state() + " \u21d2 " + pinnedLens + " = " + pin
                           + "` refutes " + outside + " \u2208 " + lens;
                }
                Set<String> inside = new LinkedHashSet<>(n);
                inside.retainAll(pin);
                if (!inside.isEmpty()) {
                    return f.op() + ": pin `" + f.state() + " \u21d2 " + pinnedLens + " = " + pin
                           + "` refutes \u00ac(" + inside + " \u2208 " + lens + ")";
                }
            }
            if (pin != null && f.subsetOf().contains(lens + "\u2286" + pinnedLens)) {
                Set<String> outside = new LinkedHashSet<>(p);
                outside.removeAll(pin);
                if (!outside.isEmpty()) {
                    return f.op() + ": `" + lens + " \u2286 " + pinnedLens + " = " + pin
                           + "` refutes " + outside + " \u2208 " + lens;
                }
            }
        }
        return null;
    }

    private static Set<String> concat(Set<String> a, Set<String> b) {
        Set<String> s = new LinkedHashSet<>(a);
        s.addAll(b);
        return s;
    }

    /**
     * Same LRE-CAM shape as the real defect: state S1 has an event-triggered
     * branch FIRST and an autonomous branch SECOND, so the autonomous branch
     * takes an absence conjunct for {@code go} — and S1's pin necessarily
     * contains {@code go}, because S1 has a {@code go}-triggered outgoing
     * transition. Pre-split, absence and pin are contradictory.
     */
    private static String generate(Path tmp) throws Exception {
        Path src = tmp.resolve("src");
        Files.createDirectories(src.resolve("sm"));
        Files.writeString(src.resolve("sm/MyMode.java"), """
                package sm;
                public enum MyMode { S1, S2, S3, S4 }
                """);
        Files.writeString(src.resolve("sm/MyEvent.java"), """
                package sm;
                public sealed interface MyEvent {
                    record Go() implements MyEvent {}
                    record Other() implements MyEvent {}
                }
                """);
        Files.writeString(src.resolve("sm/MyController.java"), """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    private boolean safe = false;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.Go) {
                                currentMode = MyMode.S2;
                            } else if (safe) {
                                currentMode = MyMode.S3;
                            }
                        } else if (currentMode == MyMode.S2) {
                            if (event instanceof MyEvent.Other) {
                                currentMode = MyMode.S4;
                            }
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
        new T2mPhase().run(ctx);
        new TransformPhase().run(ctx);
        new IsabellePhase().run(ctx);
        Path thy = output.resolve("isabelle/MyController_Beh.thy");
        assertTrue(Files.exists(thy), "theory file should exist");
        return Files.readString(thy);
    }

    /**
     * THE ENCODING. No emitted precondition may be refuted by the invariant.
     * Fails pre-fix: {@code S1ToS3} carries {@code \<not>(go \<in> triggers)}
     * while S1's pin fixes {@code triggers = {go}}.
     */
    @Test
    void noEmittedPreconditionContradictsAPin(@TempDir Path tmp) throws Exception {
        String theory = generate(tmp);
        Map<String, Set<String>> pins = pins(theory);
        String lens = pinnedLens(theory);
        assertFalse(pins.isEmpty(), "the zstore invariant must still pin per state:\n" + theory);

        List<String> dead = new ArrayList<>();
        for (Frag f : fragments(theory)) {
            String why = unsat(f, pins, lens);
            if (why != null) dead.add(why);
        }
        assertTrue(dead.isEmpty(),
                "ENCODING: every emitted precondition must be satisfiable under the "
                + "zstore invariant — an operation whose pre contradicts a pin is "
                + "PROOF-DEAD. Unsatisfiable:\n  " + String.join("\n  ", dead)
                + "\n\nTheory:\n" + theory);
    }

    /**
     * THE ANTI-VACUITY GUARD. Making the pins meaningless would also make
     * assertion 1 pass, and would be no better: an invariant that constrains
     * nothing is exactly what reviewer R2 objects to. Each pin must still cut
     * down the admissible event valuations that reach a guard — which, after the
     * split, it does through the {@code offered \<subseteq> listens} link.
     */
    @Test
    void pinsAreNotVacuous(@TempDir Path tmp) throws Exception {
        String theory = generate(tmp);
        Map<String, Set<String>> pins = pins(theory);
        String lens = pinnedLens(theory);
        assertFalse(pins.isEmpty(), "the zstore invariant must pin per state:\n" + theory);

        // Every pin must be a PROPER subset of the event alphabet — a pin equal
        // to the whole alphabet would constrain nothing.
        Matcher em = Pattern.compile("enumtype Evt = (.*)").matcher(theory);
        assertTrue(em.find(), "theory must declare the Evt enumtype:\n" + theory);
        Set<String> alphabet = new LinkedHashSet<>();
        for (String e : em.group(1).split("\\|")) {
            if (!e.isBlank()) alphabet.add(e.trim());
        }
        for (Map.Entry<String, Set<String>> p : pins.entrySet()) {
            assertFalse(p.getValue().isEmpty(),
                    "pin for " + p.getKey() + " is empty — it constrains nothing");
            assertTrue(alphabet.containsAll(p.getValue()) && !p.getValue().containsAll(alphabet),
                    "pin for " + p.getKey() + " = " + p.getValue() + " must be a PROPER subset of "
                    + alphabet + " to constrain anything");
        }

        // And the pinned lens must actually reach the guards: either a guard
        // reads it directly (pre-split encoding), or a guard's dynamic lens is
        // bounded by it via `\<subseteq>` (post-split encoding). Without one of
        // these the pins are inert and the invariant is decoration.
        boolean reaches = false;
        for (Frag f : fragments(theory)) {
            if (f.pos().containsKey(lens) || f.neg().containsKey(lens)) { reaches = true; break; }
            for (String s : f.subsetOf()) {
                if (s.endsWith("\u2286" + lens)) { reaches = true; break; }
            }
            if (reaches) break;
        }
        assertTrue(reaches,
                "VACUITY: no emitted precondition reads the pinned lens `" + lens
                + "` directly or is bounded by it via \\<subseteq>, so the per-state "
                + "pins constrain nothing any guard can see:\n" + theory);
    }

    /**
     * THE SPLIT ITSELF, and the reason it works: absence and presence must read
     * a lens that the invariant does NOT pin, or the contradiction returns.
     */
    @Test
    void guardsReadAnUnpinnedDynamicLens(@TempDir Path tmp) throws Exception {
        String theory = generate(tmp);
        String lens = pinnedLens(theory);
        assertEquals("listens", lens,
                "the invariant must pin the STATIC lens `listens`:\n" + theory);

        Set<String> guardLenses = new LinkedHashSet<>();
        for (Frag f : fragments(theory)) {
            guardLenses.addAll(f.pos().keySet());
            guardLenses.addAll(f.neg().keySet());
        }
        assertEquals(Set.of("offered"), guardLenses,
                "trigger presence and absence conjuncts must read ONLY the dynamic "
                + "lens `offered`; reading the pinned lens is what made the absence "
                + "conjunct unsatisfiable. Got: " + guardLenses + "\n" + theory);

        // `offered` must be a genuinely free input: no operation may assign it,
        // and Init must not fix it. Either would re-determine the environment.
        assertFalse(theory.contains("offered\\<Zprime>"),
                "no operation may assign `offered` — it is the environment's free "
                + "input; assigning it would make enabledness automaton-determined:\n" + theory);
        assertFalse(theory.contains("offered\\<leadsto>"),
                "Init must not fix `offered` — every run would start from one "
                + "environment valuation:\n" + theory);
        assertTrue(theory.contains("listens\\<Zprime>"),
                "operations must still write the STATIC lens `listens`, or the "
                + "per-state pins cannot be re-established:\n" + theory);
    }
}
