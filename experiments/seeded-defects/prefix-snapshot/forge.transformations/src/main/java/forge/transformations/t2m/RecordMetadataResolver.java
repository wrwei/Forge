package forge.transformations.t2m;

import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtRecord;
import spoon.reflect.declaration.CtRecordComponent;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts record component metadata from the live Spoon AST.
 *
 * <p>For each Java record found in the model, produces a list of
 * {@link FieldInfo} entries describing each component's name, Java
 * type, and (for generic collection types like {@code List<X>}) the
 * type-argument element. This metadata is injected into the ETL to
 * generate per-event CSP-M channel types and multi-field trigger
 * bindings.
 */
public class RecordMetadataResolver {

    /**
     * A single record component's metadata.
     *
     * @param name        the field's Java identifier (e.g. {@code "gs"})
     * @param javaType    the field's raw type simple name (e.g.
     *                    {@code "List"}, {@code "Loc"}, {@code "double"})
     * @param elementType for generic collection types, the simple name
     *                    of the first type argument (e.g.
     *                    {@code "GasSensor"} for {@code List<GasSensor>});
     *                    empty string when the field is not a generic
     *                    container or has no type arguments
     */
    public record FieldInfo(String name, String javaType, String elementType) {
        /**
         * Convenience constructor for legacy call sites that only know
         * about (name, javaType). Always passes an empty elementType.
         */
        public FieldInfo(String name, String javaType) {
            this(name, javaType, "");
        }

        /** True if this field is a generic collection with a known element type. */
        public boolean hasElementType() {
            return elementType != null && !elementType.isEmpty();
        }
    }

    /**
     * Walk the Spoon AST and build a map from record simple names
     * to their component metadata.
     *
     * @param spoonModel the parsed Spoon model
     * @return map from record simple name to ordered list of field info
     */
    public static Map<String, List<FieldInfo>> resolve(CtModel spoonModel) {
        Map<String, List<FieldInfo>> result = new LinkedHashMap<>();
        for (CtType<?> type : spoonModel.getAllTypes()) {
            collectRecords(type, result);
        }
        return result;
    }

    private static void collectRecords(CtType<?> type,
                                       Map<String, List<FieldInfo>> result) {
        if (type instanceof CtRecord record) {
            List<FieldInfo> fields = new ArrayList<>();
            for (CtRecordComponent comp : record.getRecordComponents()) {
                String fieldName = comp.getSimpleName();
                CtTypeReference<?> typeRef = comp.getType();
                String javaType = typeRef != null
                        ? typeRef.getSimpleName()
                        : "Object";
                String elementType = "";
                // Capture the first type argument for parameterized types
                // like List<GasSensor>, Set<X>, Collection<X>. The ETL
                // uses this to emit a RoboChart SeqType referencing the
                // proper inner type rather than synthesising a
                // "<event>_Type_<field>_List" PrimitiveType that no
                // downstream consumer can resolve.
                if (typeRef != null && !typeRef.getActualTypeArguments().isEmpty()) {
                    CtTypeReference<?> first = typeRef.getActualTypeArguments().get(0);
                    if (first != null) {
                        elementType = first.getSimpleName();
                    }
                }
                fields.add(new FieldInfo(fieldName, javaType, elementType));
            }
            if (!fields.isEmpty()) {
                result.put(record.getSimpleName(), fields);
            }
        }
        // Recurse into nested types (inner records within sealed interfaces)
        for (CtType<?> nested : type.getNestedTypes()) {
            collectRecords(nested, result);
        }
    }
}
