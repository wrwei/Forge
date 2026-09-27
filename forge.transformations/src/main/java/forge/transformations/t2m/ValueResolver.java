package forge.transformations.t2m;

import org.eclipse.emf.ecore.EObject;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import forge.transformations.core.Phase;

/**
 * Resolves literal values and named constants from the live Spoon AST,
 * producing a map from EMF EObjects to their resolved Java values.
 *
 * <p>This pre-processing step runs after Spoon parsing but uses the
 * Spoon→EMF mapping to key results by EMF EObject identity, so the
 * ETL can look up values directly from model elements.
 *
 * <p>Handles two cases:
 * <ul>
 *   <li><b>Literals</b>: {@code CtLiteral.getValue()} for integers, doubles, strings, booleans</li>
 *   <li><b>Named constants</b>: {@code static final} field reads resolved by walking all
 *       parsed types for field declarations with literal initialisers</li>
 * </ul>
 *
 * <p>Named constant resolution works within the same source tree (all files
 * parsed by Spoon). Cross-package constants from external libraries cannot
 * be resolved in no-classpath mode.
 */
public class ValueResolver {

    private static final Logger log = LoggerFactory.getLogger(ValueResolver.class);

    /**
     * Walk the Spoon AST and build a map from EMF EObjects to resolved values.
     *
     * @param spoonModel    the parsed Spoon model
     * @param spoonToEmfMap Spoon→EMF identity mapping from the mapper
     * @return map of EMF EObjects to their resolved Java values (Integer, Double, String, etc.)
     */
    public static Map<EObject, Object> resolve(CtModel spoonModel,
                                                Map<CtElement, EObject> spoonToEmfMap) {
        Map<EObject, Object> result = new IdentityHashMap<>();

        // Phase 1: Build constant lookup from all static final fields
        // Key format: "TypeSimpleName.fieldName" → literal value
        Map<String, Object> constants = buildConstantMap(spoonModel);

        // Phase 2: Walk all elements, resolve literals and field reads
        for (CtElement element : spoonModel.getElements(e -> true)) {
            EObject emf = spoonToEmfMap.get(element);
            if (emf == null) continue;

            if (element instanceof CtLiteral<?> lit) {
                Object val = lit.getValue();
                if (val instanceof Number || val instanceof String || val instanceof Boolean) {
                    result.put(emf, val);
                }
            } else if (element instanceof CtUnaryOperator<?> unary) {
                // T2M-2 fix: Spoon parses `-1` as CtUnaryOperator(NEG, CtLiteral(1)).
                // Key the NEGATED value on the unary node itself, so a lookup on the
                // initialiser expression sees -1 rather than missing (and falling back
                // to a type default) or hitting the inner literal's +1.
                Object val = foldNegatedLiteral(unary);
                if (val != null) {
                    result.put(emf, val);
                }
            } else if (element instanceof CtFieldRead<?> read) {
                Object val = resolveFieldRead(read, constants);
                if (val != null) {
                    result.put(emf, val);
                }
            }
        }
        return result;
    }

    /**
     * Build a map of named constants: "TypeName.FIELD_NAME" → literal value.
     * Also includes unqualified keys: "FIELD_NAME" → value.
     */
    public static Map<String, Object> buildConstantMap(CtModel spoonModel) {
        Map<String, Object> constants = new HashMap<>();
        for (CtType<?> type : spoonModel.getAllTypes()) {
            for (CtField<?> field : type.getFields()) {
                if (field.isStatic() && field.isFinal()) {
                    var init = field.getDefaultExpression();
                    // T2M-2 fix: accept both plain literals and negated literals
                    // (`static final int X = -1` parses as CtUnaryOperator(NEG, CtLiteral(1))).
                    Object value = null;
                    if (init instanceof CtLiteral<?> lit && lit.getValue() != null) {
                        value = lit.getValue();
                    } else if (init instanceof CtUnaryOperator<?> unary) {
                        value = foldNegatedLiteral(unary);
                    }
                    if (value != null) {
                        // Qualified key: "TypeName.FIELD_NAME"
                        String qualifiedKey = type.getSimpleName() + "." + field.getSimpleName();
                        constants.put(qualifiedKey, value);
                        // Unqualified key: "FIELD_NAME" (fallback for unresolved declaring types).
                        // T2M-3 fix: warn on collision instead of silently keeping the
                        // first parsed type's value — the winner depends on Spoon's
                        // parse order, which is not a semantic property of the input.
                        Object prior = constants.putIfAbsent(field.getSimpleName(), value);
                        if (prior != null && !prior.equals(value)) {
                            log.warn("Constant name collision on unqualified key '{}': "
                                    + "kept {} (first parsed), ignoring {} from {}. "
                                    + "Unqualified fallback lookups are parse-order dependent "
                                    + "for this name; qualified lookups are unaffected.",
                                    field.getSimpleName(), prior, value, type.getSimpleName());
                        }
                    }
                }
            }
        }
        return constants;
    }

    /**
     * Fold {@code CtUnaryOperator(NEG, CtLiteral(n))} to the negated numeric value.
     * Returns {@code null} for any other shape (other operators, non-numeric operands,
     * nested expressions), leaving those to the ETL's structural translation.
     */
    private static Object foldNegatedLiteral(CtUnaryOperator<?> unary) {
        if (unary.getKind() != UnaryOperatorKind.NEG) {
            return null;
        }
        CtExpression<?> operand = unary.getOperand();
        if (operand instanceof CtLiteral<?> lit && lit.getValue() instanceof Number n) {
            if (n instanceof Integer i) return -i;
            if (n instanceof Long l) return -l;
            if (n instanceof Double d) return -d;
            if (n instanceof Float f) return -f;
            if (n instanceof Short s) return (int) -s;
            if (n instanceof Byte b) return (int) -b;
        }
        return null;
    }

    private static Object resolveFieldRead(CtFieldRead<?> read, Map<String, Object> constants) {
        String fieldName = read.getVariable().getSimpleName();

        // Try qualified lookup first: "DeclaringType.fieldName"
        CtTypeReference<?> declaringType = read.getVariable().getDeclaringType();
        if (declaringType != null) {
            String typeName = declaringType.getSimpleName();
            Object val = constants.get(typeName + "." + fieldName);
            if (val != null) return val;
        }

        // Fallback: unqualified lookup by field name alone
        return constants.get(fieldName);
    }
}
