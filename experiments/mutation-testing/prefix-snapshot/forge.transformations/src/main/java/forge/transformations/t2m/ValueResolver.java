package forge.transformations.t2m;

import org.eclipse.emf.ecore.EObject;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

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
                    if (init instanceof CtLiteral<?> lit && lit.getValue() != null) {
                        // Qualified key: "TypeName.FIELD_NAME"
                        String qualifiedKey = type.getSimpleName() + "." + field.getSimpleName();
                        constants.put(qualifiedKey, lit.getValue());
                        // Unqualified key: "FIELD_NAME" (fallback for unresolved declaring types)
                        constants.putIfAbsent(field.getSimpleName(), lit.getValue());
                    }
                }
            }
        }
        return constants;
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
