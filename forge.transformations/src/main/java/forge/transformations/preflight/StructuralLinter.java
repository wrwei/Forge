package forge.transformations.preflight;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pre-flight structural linter for the generated Java project.
 *
 * <p>Detects shape-rule violations that don't necessarily crash the
 * ETL/EGL transformation but produce an incorrect RoboChart model
 * (silent-model-defect class). These rules come from
 * {@code forge.assets/prompts/java_codegen_rules.txt} and CLAUDE.md.
 *
 * <p>Output: {@code lint_report.json} with a flat array of violations.
 * The dashboard reads this and emits {@code post_preflight.md/.json}.
 */
public class StructuralLinter {

    private static final String ROBOCHART_TYPE = "RoboChartType";

    private final List<Map<String, Object>> violations = new ArrayList<>();

    public void lint(Path sourceRoot) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setComplianceLevel(17);
        launcher.getEnvironment().setShouldCompile(false);
        launcher.addInputResource(sourceRoot.toAbsolutePath().toString());
        launcher.buildModel();
        CtModel model = launcher.getModel();

        int typesScanned = 0;
        for (CtType<?> type : model.getAllTypes()) {
            typesScanned++;
            checkType(type);
        }
        // Empty-model guard: a wrong source path (or an empty generated
        // project) must NOT produce a clean report — zero types scanned
        // means the linter verified nothing, so "0 violations" would be
        // a vacuous pass.
        if (typesScanned == 0) {
            add("rule0_no_types_found", "error",
                    null,
                    "No Java types found under source root '" + sourceRoot + "'",
                    "The linter parsed zero types, so no structural rule was actually "
                    + "checked. Verify the preflight 'source' argument points at the "
                    + "generated project's Java source root and that code generation "
                    + "ran before preflight. A clean lint report over an empty model "
                    + "is not evidence of structural conformance.");
        }
    }

    public List<Map<String, Object>> getViolations() {
        return violations;
    }

    public void writeReport(Path outputFile) throws IOException {
        try (Writer w = Files.newBufferedWriter(outputFile)) {
            w.write("{\n  \"violations\": [\n");
            for (int i = 0; i < violations.size(); i++) {
                Map<String, Object> v = violations.get(i);
                w.write("    {");
                boolean first = true;
                for (Map.Entry<String, Object> kv : v.entrySet()) {
                    if (!first) w.write(", ");
                    w.write("\"" + kv.getKey() + "\": ");
                    Object val = kv.getValue();
                    if (val instanceof Integer || val instanceof Long) {
                        w.write(val.toString());
                    } else {
                        w.write("\"" + escapeJson(String.valueOf(val)) + "\"");
                    }
                    first = false;
                }
                w.write("}");
                if (i < violations.size() - 1) w.write(",");
                w.write("\n");
            }
            w.write("  ]\n}\n");
        }
    }

    private void checkType(CtType<?> type) {
        boolean hasStep = false;
        for (CtMethod<?> m : type.getMethods()) {
            String name = m.getSimpleName();
            if ("compute".equals(name)) {
                checkComputeMethod(type, m);
            } else if ("step".equals(name)) {
                hasStep = true;
                checkStepMethod(type, m);
            }
            // Rule 4: double parameters must have @RoboChartType("real")
            for (CtParameter<?> p : m.getParameters()) {
                checkDoubleAnnotation(p, p.getType() == null ? "" : p.getType().getSimpleName(),
                        "parameter", type.getSimpleName() + "." + m.getSimpleName() + "#" + p.getSimpleName());
            }
        }
        // Rule 4: double fields must have @RoboChartType("real")
        for (CtField<?> f : type.getFields()) {
            checkDoubleAnnotation(f, f.getType() == null ? "" : f.getType().getSimpleName(),
                    "field", type.getSimpleName() + "." + f.getSimpleName());
        }
        // Rule 7: signed sentinel values on controller state (advisory)
        if (hasStep) {
            checkSignedSentinels(type);
        }
        // Rule 5: @RoboChartType on local variables is banned (target doesn't allow it)
        for (CtLocalVariable<?> lv : type.getElements(new TypeFilter<>(CtLocalVariable.class))) {
            if (hasAnnotation(lv, ROBOCHART_TYPE)) {
                add("rule5_robochart_on_local", "error",
                        lv,
                        "@RoboChartType applied on local variable '" + lv.getSimpleName() + "'",
                        "Remove @RoboChartType from the local variable. The annotation's @Target "
                        + "permits only FIELD, PARAMETER, and METHOD — using it on a local "
                        + "variable will fail to compile, and local variables do not appear "
                        + "in the RoboChart model anyway.");
            }
        }
    }

    // ── Rule 1: compute() has local variables ─────────────────────────

    private void checkComputeMethod(CtType<?> owner, CtMethod<?> m) {
        if (m.getBody() == null) return;
        List<CtLocalVariable<?>> locals = m.getBody().getElements(
                new TypeFilter<>(CtLocalVariable.class));
        for (CtLocalVariable<?> lv : locals) {
            add("rule1_compute_local_variable", "error",
                    lv,
                    owner.getSimpleName() + ".compute() declares local variable '"
                            + lv.getSimpleName() + "'",
                    "Remove local variables from compute(). Each statement in compute() must be "
                    + "'this.field = expression' only. Move the value into a field of this class "
                    + "(assigned on the same compute() call before it is read), or inline the "
                    + "expression. See java_codegen_rules.txt.");
        }
    }

    // ── Rules 2 + 3: step() outer shape + named predicates ──────────────

    private void checkStepMethod(CtType<?> owner, CtMethod<?> m) {
        if (m.getBody() == null) return;

        // Count boolean locals declared at the top of the method (before the first if).
        boolean sawIf = false;
        int booleanLocalsBeforeIf = 0;
        CtIf outerIf = null;
        for (CtStatement s : m.getBody().getStatements()) {
            if (!sawIf && s instanceof CtLocalVariable<?> lv) {
                String tn = lv.getType() == null ? "" : lv.getType().getSimpleName();
                if ("boolean".equalsIgnoreCase(tn) || "Boolean".equals(tn)) {
                    booleanLocalsBeforeIf++;
                }
            } else if (s instanceof CtIf ifStmt) {
                if (!sawIf) {
                    outerIf = ifStmt;
                    sawIf = true;
                }
            }
        }

        // Rule 2: flag any if in step() whose condition is `currentMode != X`.
        // We scan ALL if nodes (not just the outer chain) so we catch
        // else-if chains regardless of how Spoon wraps them (CtIf vs
        // CtBlock-containing-a-single-CtIf).
        for (CtIf ifStmt : m.getBody().getElements(new TypeFilter<>(CtIf.class))) {
            checkOuterCondition(owner, ifStmt);
        }

        // Rule 6: priority-negation obligation on if/else-if chains.
        // Walk every chain HEAD in step() (an if that is not itself the
        // else-branch of another if) and check the negation convention.
        for (CtIf ifStmt : m.getBody().getElements(new TypeFilter<>(CtIf.class))) {
            if (!isElseBranchOfAnotherIf(ifStmt)) {
                checkPriorityNegations(owner, ifStmt);
            }
        }

        // Rule 3: if there are method invocations inside any if-condition and
        // zero boolean predicates were declared, flag as a warning.
        if (booleanLocalsBeforeIf == 0) {
            List<CtIf> allIfs = m.getBody().getElements(new TypeFilter<>(CtIf.class));
            boolean anyInvocationInGuards = false;
            for (CtIf ifs : allIfs) {
                CtExpression<?> cond = ifs.getCondition();
                if (cond == null) continue;
                if (!cond.getElements(new TypeFilter<>(CtInvocation.class)).isEmpty()) {
                    anyInvocationInGuards = true;
                    break;
                }
            }
            if (anyInvocationInGuards) {
                add("rule3_step_no_named_predicates", "warning",
                        m,
                        owner.getSimpleName() + ".step() has method calls inside if-conditions "
                                + "but declares no named boolean predicates above the if-else chain",
                        "Extract each guard into a named boolean local variable declared at the top "
                        + "of step(), before the outer if-else chain. Use descriptive names that "
                        + "encode the sensor function and threshold (e.g. 'boolean velBelowLimit = "
                        + "calcVel.speed() <= MAX_SPEED;'). See java_codegen_rules.txt "
                        + "'NAMED BOOLEAN PREDICATES'.");
            }
        }
    }

    private void checkOuterCondition(CtType<?> owner, CtIf ifStmt) {
        CtExpression<?> cond = ifStmt.getCondition();
        if (!(cond instanceof CtBinaryOperator<?> bop)) return;
        // Spoon 11.3.0 (noclasspath) leaves getKind() null on `!=` nodes,
        // so `kind != NE → return` silently skipped every real violation.
        // Accept NE, or a null kind whose source text contains `!=`.
        boolean isNe = bop.getKind() == BinaryOperatorKind.NE
                || (bop.getKind() == null && exprText(bop).contains("!="));
        if (!isNe) return;

        String lhs = exprText(bop.getLeftHandOperand());
        String rhs = exprText(bop.getRightHandOperand());
        // The ETL naming table (java_codegen_rules.txt) recognises the mode
        // field under any of `currentMode`, `mode`, `status` — match all
        // three (word-bounded, `this.`-tolerant), not just `currentMode`.
        if (mentionsModeField(lhs) || mentionsModeField(rhs)) {
            add("rule2_step_outer_ne", "error",
                    ifStmt,
                    owner.getSimpleName() + ".step() outer if uses '<modeField> != X'",
                    "Replace the '!=' branch with explicit 'currentMode == X' blocks — one block "
                    + "per mode. The formal model extraction requires every outer branch to be "
                    + "'currentMode == <Mode>' so each mode maps to exactly one source state. "
                    + "See java_codegen_rules.txt 'PURE TWO-LEVEL IF-ELSE'.");
        }
    }

    // ── Rule 6: priority negations in if/else-if chains (warning) ───────
    //
    // Hand-written LRE convention: within a step() if/else-if chain, every
    // branch below a TRIGGERLESS guarded branch (a guard with no
    // `instanceof` event test) must textually carry the negation of each
    // preceding triggerless guard, e.g.
    //     if (camActive) {...}
    //     else if (hcmActive && !camActive) {...}
    //     else if (inOpez && !camActive && !hcmActive) {...}
    // Sources that rely on the implicit priority of else-if produce formal
    // models whose operations fire nondeterministically (Tier-B conformance
    // gap C1). WARNING severity: the M2M track is landing synthesis of the
    // negations, so this rule documents the obligation rather than blocks.

    private void checkPriorityNegations(CtType<?> owner, CtIf chainHead) {
        List<CtIf> chain = new ArrayList<>();
        for (CtIf cur = chainHead; cur != null; cur = nextElseIf(cur)) {
            chain.add(cur);
        }
        if (chain.size() < 2) return;

        // Cores of preceding triggerless guards (normalised, negations of
        // still-earlier guards stripped so only the positive atom remains).
        List<String> precedingCores = new ArrayList<>();
        // S2 FIX — event-triggered predecessors.
        //
        // Defect: this loop only ever added TRIGGERLESS guards to
        // precedingCores, so rule6 could not fire on an instance of this shape by
        // construction. The LRE CAM block
        //     if (event instanceof ReqOCM) { ... }
        //     else if (cdaAboveOrAtMinSafe) { ... }
        // is exactly the shape the C1 obligation exists to catch, and it
        // produced no finding in any run.
        //
        // The blocking relation is DIRECTIONAL, which is why these are tracked
        // separately from precedingCores rather than folded into them:
        //   * event-triggered BEFORE event-triggered — no obligation: the two
        //     branches are already disjoint because at most one event is
        //     offered per step, so trigger disjointness does the blocking;
        //   * event-triggered BEFORE triggerless — OBLIGATION:
        //     the later branch has no trigger, so nothing makes the two
        //     mutually exclusive, and both become enabled in the extracted
        //     model where the Java takes only the first.
        //
        // Reported as a distinct rule id rather than reusing rule6's message,
        // because the remedy differs. rule6 says "conjoin && !<guard>"; here
        // the earlier branch contributes no data predicate to negate — its
        // firing condition is the PRESENCE OF AN EVENT, and RoboChart's
        // expression language has no event-absence form (robochart.ecore's
        // Communication metaclass models only a positive trigger). So the
        // remedy is a source-level one: give the later branch a guard that
        // excludes the earlier branch's case, or reorder the chain.
        List<String> precedingEventBranches = new ArrayList<>();
        for (CtIf branch : chain) {
            CtExpression<?> cond = branch.getCondition();
            if (cond == null) continue;
            String condText = exprText(cond);
            String norm = normalizeGuard(condText);
            boolean triggerless = isTriggerlessGuard(cond);

            if (triggerless && !precedingEventBranches.isEmpty()) {
                add("rule8_event_branch_precedes_triggerless", "warning",
                        branch,
                        owner.getSimpleName() + ".step(): triggerless branch guarded by '"
                                + condText + "' follows event-triggered branch(es): "
                                + String.join(", ", precedingEventBranches)
                                + " — in the extracted model both are enabled together",
                        "An event-triggered branch followed by a TRIGGERLESS branch in the "
                        + "same else-if chain has no counterpart in RoboChart: the Java "
                        + "takes only the first branch, but the extracted transitions form "
                        + "an unguarded external choice and the triggerless one is enabled "
                        + "whenever its data guard holds — including when the event is "
                        + "present. Trigger disjointness does NOT close this (it only "
                        + "separates two EVENT-triggered branches), and the negation "
                        + "synthesis that closes rule6 cannot either, because the "
                        + "event-triggered branch contributes no data predicate to negate. "
                        + "RoboChart has no event-absence guard, so this must be fixed in "
                        + "the Java: either give the later branch an explicit guard that "
                        + "excludes the earlier branch's case, or place the triggerless "
                        + "branch FIRST so the ordinary priority-negation synthesis applies. "
                        + "Witness: LreController.java CAM block (reqOCM then "
                        + "cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two "
                        + "operations where the Java is deterministic.");
            }

            if (!precedingCores.isEmpty()) {
                List<String> missing = new ArrayList<>();
                for (String core : precedingCores) {
                    if (!carriesNegationOf(norm, core)) {
                        missing.add(core);
                    }
                }
                if (!missing.isEmpty()) {
                    add("rule6_missing_priority_negation", "warning",
                            branch,
                            owner.getSimpleName() + ".step(): branch guarded by '"
                                    + condText + "' does not negate preceding triggerless guard(s): "
                                    + String.join(", ", missing),
                            "Branches below a triggerless guarded branch must textually carry "
                            + "the negation of each preceding triggerless guard (e.g. "
                            + "'else if (hcmActive && !camActive)'). Relying on the implicit "
                            + "priority of else-if produces a formal model whose operations "
                            + "can fire nondeterministically — the extracted preconditions "
                            + "are not mutually exclusive. Conjoin '&& !<guard>' for each "
                            + "guard listed above. See the hand-written LRE convention in "
                            + "reference-runs/lre/java/controller/LreController.java.");
                }
            }

            if (triggerless && !mentionsModeDispatch(norm)) {
                precedingCores.add(stripKnownNegations(norm, precedingCores));
            } else if (!triggerless) {
                // S2: record the event-triggered branch so a LATER triggerless
                // branch in this chain is flagged (see the rule8 block above).
                precedingEventBranches.add(condText);
            }
        }
    }

    /** Follow the else-if chain: the else branch is either a CtIf directly
     * or a CtBlock whose single statement is a CtIf (implicit block). */
    private static CtIf nextElseIf(CtIf ifStmt) {
        CtStatement els = ifStmt.getElseStatement();
        if (els instanceof CtIf next) return next;
        if (els instanceof CtBlock<?> blk
                && blk.getStatements().size() == 1
                && blk.getStatement(0) instanceof CtIf next) {
            return next;
        }
        return null;
    }

    /** True when this if is the else-branch (or else-if) of another if. */
    private static boolean isElseBranchOfAnotherIf(CtIf ifStmt) {
        CtElement parent = ifStmt.getParent();
        if (parent instanceof CtIf p) {
            return p.getElseStatement() == ifStmt;
        }
        if (parent instanceof CtBlock<?> blk && blk.getParent() instanceof CtIf p) {
            return p.getElseStatement() == blk && blk.getStatements().size() == 1;
        }
        return false;
    }

    /** Triggerless = the guard contains no `instanceof` event test. */
    private static boolean isTriggerlessGuard(CtExpression<?> cond) {
        if (cond instanceof CtBinaryOperator<?> b
                && b.getKind() == BinaryOperatorKind.INSTANCEOF) {
            return false;
        }
        for (CtBinaryOperator<?> b : cond.getElements(new TypeFilter<>(CtBinaryOperator.class))) {
            if (b.getKind() == BinaryOperatorKind.INSTANCEOF) return false;
        }
        return exprText(cond).indexOf(" instanceof ") < 0;
    }

    /** Mode-dispatch guards (`currentMode == X`, `mode == X`, `status == X`)
     * are handled by rule 2 and are mutually exclusive by construction —
     * they don't accumulate a negation obligation. */
    private static boolean mentionsModeDispatch(String normalizedGuard) {
        return java.util.regex.Pattern
                .compile("\\b(currentMode|mode|status)==")
                .matcher(normalizedGuard).find();
    }

    /** Word-bounded match for the ETL-recognised mode field names
     * (`currentMode`, `mode`, `status`), tolerant of `this.` prefixes. */
    private static boolean mentionsModeField(String expr) {
        return java.util.regex.Pattern
                .compile("\\b(currentMode|mode|status)\\b")
                .matcher(expr.replace("this.", "")).find();
    }

    /** Normalise a guard for textual comparison: drop whitespace and
     * `this.` qualifiers so `this.cstc` and `cstc` compare equal, and
     * unwrap parens around simple (possibly negated) atoms, because
     * Spoon pretty-prints `!camActive` as `(!camActive)`. */
    private static String normalizeGuard(String s) {
        String out = s.replace(" ", "").replace("\n", "").replace("\t", "")
                .replace("this.", "");
        String prev;
        do {
            prev = out;
            out = out.replaceAll("\\((!?[A-Za-z_$][A-Za-z0-9_$]*)\\)", "$1");
        } while (!out.equals(prev));
        return out;
    }

    /** Remove `!core` / `!(core)` conjuncts of earlier guards so only the
     * branch's own positive atom(s) remain as its core. */
    private static String stripKnownNegations(String norm, List<String> earlierCores) {
        String out = norm;
        for (String core : earlierCores) {
            out = out.replace("&&!(" + core + ")", "")
                     .replace("!(" + core + ")&&", "")
                     .replace("&&!" + core, "")
                     .replace("!" + core + "&&", "");
        }
        // Unwrap a single redundant outer paren pair if present.
        if (out.startsWith("(") && out.endsWith(")")) {
            String inner = out.substring(1, out.length() - 1);
            if (isBalanced(inner)) out = inner;
        }
        return out;
    }

    private static boolean isBalanced(String s) {
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') depth++;
            else if (c == ')' && --depth < 0) return false;
        }
        return depth == 0;
    }

    /** Does the normalised guard textually carry `!core` or `!(core)`? */
    private static boolean carriesNegationOf(String normalizedGuard, String core) {
        return normalizedGuard.contains("!" + core)
                || normalizedGuard.contains("!(" + core + ")");
    }

    // ── Rule 7: signed sentinel values on controller state (warning) ────
    //
    // History: Java `int` maps to RoboChart `nat` in the M2M type mapping,
    // so a negative sentinel (`cstc = -1`) silently leaves the RoboChart
    // type's domain and the formal model diverges from the Java. The M2M
    // track is switching int -> RoboChart int; this rule stays advisory so
    // the obligation is documented without blocking.

    private static final Set<String> INTEGER_TYPE_NAMES = new HashSet<>(List.of(
            "int", "Integer", "long", "Long", "short", "Short", "byte", "Byte"));

    private void checkSignedSentinels(CtType<?> type) {
        // Controller state = integer-typed fields of the class containing step().
        Set<String> intFields = new HashSet<>();
        for (CtField<?> f : type.getFields()) {
            String tn = f.getType() == null ? "" : f.getType().getSimpleName();
            if (INTEGER_TYPE_NAMES.contains(tn)) {
                intFields.add(f.getSimpleName());
            }
        }
        if (intFields.isEmpty()) return;

        // (a) field declaration initialisers: `private int cstc = -1;`
        for (CtField<?> f : type.getFields()) {
            if (!intFields.contains(f.getSimpleName())) continue;
            if (isNegativeIntLiteral(f.getDefaultExpression())) {
                addRule7(type, f, f.getSimpleName(),
                        "initialised with negative literal "
                                + f.getDefaultExpression());
            }
        }
        // (b) assignments anywhere in the class: `this.cstc = -1;`
        for (CtAssignment<?, ?> asg : type.getElements(new TypeFilter<>(CtAssignment.class))) {
            String target = fieldNameOf(asg.getAssigned(), intFields);
            if (target != null && isNegativeIntLiteral(asg.getAssignment())) {
                addRule7(type, asg, target,
                        "assigned negative literal " + asg.getAssignment());
            }
        }
        // (c) comparisons: `cstc == -1`, `-1 != this.cdyn`, `cstc >= -1`, ...
        // NOTE: Spoon 11.3.0 noclasspath leaves getKind() null on `!=`
        // nodes, so a null kind is treated as a comparison when the source
        // text contains `!=` (skipping it would false-pass NE sentinels).
        for (CtBinaryOperator<?> b : type.getElements(new TypeFilter<>(CtBinaryOperator.class))) {
            BinaryOperatorKind kind = b.getKind();
            if (kind == null) {
                if (!exprText(b).contains("!=")) continue; // genuinely unknown node
            } else {
                switch (kind) {
                    case EQ: case NE: case LT: case LE: case GT: case GE:
                        break;
                    default:
                        continue;
                }
            }
            String lhsField = fieldNameOf(b.getLeftHandOperand(), intFields);
            String rhsField = fieldNameOf(b.getRightHandOperand(), intFields);
            if (lhsField != null && isNegativeIntLiteral(b.getRightHandOperand())) {
                addRule7(type, b, lhsField, "compared against negative literal: " + exprText(b));
            } else if (rhsField != null && isNegativeIntLiteral(b.getLeftHandOperand())) {
                addRule7(type, b, rhsField, "compared against negative literal: " + exprText(b));
            }
        }
    }

    private void addRule7(CtType<?> owner, CtElement at, String fieldName, String detail) {
        add("rule7_signed_sentinel_on_controller_state", "warning",
                at,
                owner.getSimpleName() + ": controller state field '" + fieldName
                        + "' " + detail,
                "Integer controller-state fields historically mapped to RoboChart 'nat', "
                + "so a negative sentinel silently leaves the formal type's domain and "
                + "the model diverges from the Java. Prefer a non-negative sentinel, an "
                + "Optional/enum encoding, or confirm the M2M maps this field to a signed "
                + "RoboChart 'int' before relying on negative values. See "
                + "java_codegen_rules.txt and the int->nat history in the M2M type map.");
    }

    /** Field-access match tolerant of `this.` qualification: returns the
     * field name when the expression is `f` or `this.f` for f in fields. */
    private static String fieldNameOf(CtExpression<?> expr, Set<String> fields) {
        if (expr == null) return null;
        String norm = normalizeGuard(exprText(expr));
        return fields.contains(norm) ? norm : null;
    }

    /** Matches `-<int literal>` (Spoon models -1 as unary minus on 1)
     * and, defensively, an int literal with a negative value. */
    private static boolean isNegativeIntLiteral(CtExpression<?> expr) {
        if (expr instanceof CtUnaryOperator<?> u
                && u.getKind() == UnaryOperatorKind.NEG
                && u.getOperand() instanceof CtLiteral<?> lit) {
            Object v = lit.getValue();
            return v instanceof Integer || v instanceof Long
                    || v instanceof Short || v instanceof Byte;
        }
        if (expr instanceof CtLiteral<?> lit) {
            Object v = lit.getValue();
            if (v instanceof Integer i) return i < 0;
            if (v instanceof Long l) return l < 0;
            if (v instanceof Short s) return s < 0;
            if (v instanceof Byte b) return b < 0;
        }
        return false;
    }

    // ── Rule 4: double field/parameter without @RoboChartType("real") ─

    private void checkDoubleAnnotation(CtElement elem, String typeName, String kind, String qualifiedName) {
        if (!("double".equals(typeName) || "Double".equals(typeName))) return;
        if (hasAnnotation(elem, ROBOCHART_TYPE)) return;
        add("rule4_double_missing_real_annotation", "error",
                elem,
                kind + " '" + qualifiedName + "' is a double without @RoboChartType(\"real\")",
                "Annotate with @RoboChartType(\"real\"). Without the annotation, the formal model "
                + "extraction cannot confidently map Java double to RoboChart real and may "
                + "produce an incorrect model type. See java_codegen_rules.txt.");
    }

    // ── Helpers ────────────────────────────────────────────────────────

    /**
     * Stringify an expression defensively. Spoon 11.3.0 in noclasspath
     * mode leaves {@code getKind() == null} on {@code !=} binary
     * operators, and pretty-printing such a node throws NPE — so
     * {@code toString()} on any guard containing {@code !=} crashes.
     * Fall back to the original source fragment via the position.
     */
    static String exprText(CtExpression<?> expr) {
        if (expr == null) return "";
        try {
            return expr.toString();
        } catch (RuntimeException e) {
            var pos = expr.getPosition();
            if (pos != null && pos.isValidPosition() && pos.getCompilationUnit() != null) {
                String src = pos.getCompilationUnit().getOriginalSourceCode();
                if (src != null && pos.getSourceStart() >= 0
                        && pos.getSourceEnd() < src.length()) {
                    return src.substring(pos.getSourceStart(), pos.getSourceEnd() + 1);
                }
            }
            return "";
        }
    }

    private boolean hasAnnotation(CtElement elem, String simpleName) {
        if (elem == null || elem.getAnnotations() == null) return false;
        for (CtAnnotation<?> a : elem.getAnnotations()) {
            if (a.getAnnotationType() == null) continue;
            if (simpleName.equals(a.getAnnotationType().getSimpleName())) return true;
        }
        return false;
    }

    private void add(String rule, String severity, CtElement at,
                     String message, String directive) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("rule", rule);
        v.put("severity", severity);
        v.put("message", message);
        v.put("directive", directive);
        if (at != null && at.getPosition() != null && at.getPosition().isValidPosition()) {
            v.put("file", at.getPosition().getFile() == null ? ""
                    : at.getPosition().getFile().getAbsolutePath().replace("\\", "/"));
            v.put("line_start", at.getPosition().getLine());
            v.put("line_end", at.getPosition().getEndLine());
        } else {
            v.put("file", "");
            v.put("line_start", 0);
            v.put("line_end", 0);
        }
        violations.add(v);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
