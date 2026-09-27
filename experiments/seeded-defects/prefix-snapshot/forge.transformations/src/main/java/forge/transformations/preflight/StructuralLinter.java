package forge.transformations.preflight;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        for (CtType<?> type : model.getAllTypes()) {
            checkType(type);
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
        for (CtMethod<?> m : type.getMethods()) {
            String name = m.getSimpleName();
            if ("compute".equals(name)) {
                checkComputeMethod(type, m);
            } else if ("step".equals(name)) {
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
        if (bop.getKind() != BinaryOperatorKind.NE) return;

        String lhs = bop.getLeftHandOperand() == null ? "" : bop.getLeftHandOperand().toString();
        String rhs = bop.getRightHandOperand() == null ? "" : bop.getRightHandOperand().toString();
        if (lhs.contains("currentMode") || rhs.contains("currentMode")) {
            add("rule2_step_outer_ne", "error",
                    ifStmt,
                    owner.getSimpleName() + ".step() outer if uses 'currentMode != X'",
                    "Replace the '!=' branch with explicit 'currentMode == X' blocks — one block "
                    + "per mode. The formal model extraction requires every outer branch to be "
                    + "'currentMode == <Mode>' so each mode maps to exactly one source state. "
                    + "See java_codegen_rules.txt 'PURE TWO-LEVEL IF-ELSE'.");
        }
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
