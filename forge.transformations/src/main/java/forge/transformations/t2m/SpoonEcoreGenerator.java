package forge.transformations.t2m;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import spoon.metamodel.ConceptKind;
import spoon.metamodel.Metamodel;
import spoon.metamodel.MetamodelConcept;
import spoon.metamodel.MetamodelProperty;
import spoon.reflect.meta.ContainerKind;
import spoon.reflect.reference.CtTypeReference;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Generates an Ecore metamodel ({@code spoon.ecore}) from Spoon's self-description API.
 *
 * <p>Uses {@link Metamodel#getInstance()} to enumerate all Spoon metaclasses
 * and their properties, then creates corresponding EMF EClasses and features.
 *
 * <p>Run {@code main()} to regenerate {@code spoon.ecore} when the Spoon version changes.
 */
public class SpoonEcoreGenerator {

    public static final String NS_URI = "http://spoon.gforge.inria.fr/spoon";
    public static final String NS_PREFIX = "spoon";

    private final EcoreFactory ef = EcoreFactory.eINSTANCE;
    private final EcorePackage ep = EcorePackage.eINSTANCE;

    private final Map<String, EClass> classMap = new LinkedHashMap<>();
    private final Map<String, EEnum> enumMap = new LinkedHashMap<>();

    public EPackage generate() {
        EPackage pkg = ef.createEPackage();
        pkg.setName("spoon");
        pkg.setNsURI(NS_URI);
        pkg.setNsPrefix(NS_PREFIX);

        Metamodel mm = Metamodel.getInstance();
        Collection<MetamodelConcept> concepts = mm.getConcepts();

        // Pass 1: create EClasses
        for (MetamodelConcept concept : concepts) {
            EClass ec = ef.createEClass();
            String name = concept.getMetamodelInterface().getSimpleName();
            ec.setName(name);
            ec.setAbstract(concept.getKind() == ConceptKind.ABSTRACT);
            pkg.getEClassifiers().add(ec);
            classMap.put(name, ec);
        }

        // Pass 2: wire eSuperTypes
        for (MetamodelConcept concept : concepts) {
            String name = concept.getMetamodelInterface().getSimpleName();
            EClass ec = classMap.get(name);
            for (MetamodelConcept superConcept : concept.getSuperConcepts()) {
                String superName = superConcept.getMetamodelInterface().getSimpleName();
                EClass superEc = classMap.get(superName);
                if (superEc != null && !ec.getESuperTypes().contains(superEc)) {
                    ec.getESuperTypes().add(superEc);
                }
            }
        }

        // Pass 3: create EStructuralFeatures
        for (MetamodelConcept concept : concepts) {
            String conceptName = concept.getMetamodelInterface().getSimpleName();
            EClass ec = classMap.get(conceptName);

            for (MetamodelProperty prop : concept.getProperties()) {
                if (prop.isDerived()) continue;
                if (prop.isUnsettable()) continue;

                // Skip inherited properties (check Spoon ownership)
                String ownerName = prop.getOwner().getMetamodelInterface().getSimpleName();
                if (!ownerName.equals(conceptName)) continue;

                createFeature(pkg, ec, prop);
            }
        }

        // Pass 4: remove duplicate features (same name defined on a class and its supertype)
        // This handles order-dependency from Pass 3 where supertypes might not have
        // their features yet when subtype features are being added.
        for (EClass ec : classMap.values()) {
            List<EStructuralFeature> toRemove = new ArrayList<>();
            for (EStructuralFeature feature : ec.getEStructuralFeatures()) {
                if (hasSuperFeature(ec, feature.getName())) {
                    toRemove.add(feature);
                }
            }
            ec.getEStructuralFeatures().removeAll(toRemove);
        }

        return pkg;
    }

    private void createFeature(EPackage pkg, EClass owner, MetamodelProperty prop) {
        String featureName = prop.getName();

        // Get the item type qualified name via Spoon's type reference
        CtTypeReference<?> itemTypeRef = prop.getTypeofItems();
        if (itemTypeRef == null) return;

        String qualifiedName = itemTypeRef.getQualifiedName();
        if (qualifiedName == null || qualifiedName.isEmpty()) return;

        if (isPrimitive(qualifiedName)) {
            EAttribute attr = ef.createEAttribute();
            attr.setName(featureName);
            attr.setEType(mapPrimitiveType(qualifiedName));
            setMultiplicity(attr, prop.getContainerKind());
            owner.getEStructuralFeatures().add(attr);
        } else if (isSpoonEnum(qualifiedName)) {
            EAttribute attr = ef.createEAttribute();
            attr.setName(featureName);
            attr.setEType(getOrCreateEnumFromName(pkg, qualifiedName));
            setMultiplicity(attr, prop.getContainerKind());
            owner.getEStructuralFeatures().add(attr);
        } else if (isSpoonElement(qualifiedName)) {
            EReference ref = ef.createEReference();
            ref.setName(featureName);

            // Find the target EClass by simple name
            String simpleName = simpleNameOf(qualifiedName);
            EClass targetEc = classMap.get(simpleName);
            ref.setEType(targetEc != null ? targetEc : classMap.get("CtElement"));

            // Spoon's AST is a tree — every child node (including references like
            // CtFieldReference, CtTypeReference) is owned by exactly one parent.
            // Mark all references as containment so Epsilon can resolve their properties.
            ref.setContainment(true);

            setMultiplicity(ref, prop.getContainerKind());
            owner.getEStructuralFeatures().add(ref);
        }
        // else: unknown type (Object, Map, etc.), skip
    }

    private boolean isPrimitive(String qualifiedName) {
        return switch (qualifiedName) {
            case "java.lang.String", "String",
                 "boolean", "java.lang.Boolean",
                 "int", "java.lang.Integer",
                 "long", "java.lang.Long",
                 "double", "java.lang.Double",
                 "float", "java.lang.Float",
                 "byte", "char", "short" -> true;
            default -> false;
        };
    }

    private EClassifier mapPrimitiveType(String qualifiedName) {
        return switch (qualifiedName) {
            case "java.lang.String", "String" -> ep.getEString();
            case "boolean", "java.lang.Boolean" -> ep.getEBoolean();
            case "int", "java.lang.Integer" -> ep.getEInt();
            case "long", "java.lang.Long" -> ep.getELong();
            case "double", "java.lang.Double" -> ep.getEDouble();
            case "float", "java.lang.Float" -> ep.getEFloat();
            default -> ep.getEString();
        };
    }

    private boolean isSpoonElement(String qualifiedName) {
        return qualifiedName.startsWith("spoon.reflect.");
    }

    private boolean isSpoonEnum(String qualifiedName) {
        // Spoon enums are typically in spoon.reflect.code or spoon.reflect.declaration
        // and end with "Kind" (BinaryOperatorKind, UnaryOperatorKind, ModifierKind, etc.)
        if (!qualifiedName.startsWith("spoon.")) return false;
        String simpleName = simpleNameOf(qualifiedName);
        return simpleName.endsWith("Kind") || simpleName.endsWith("Modifier");
    }

    @SuppressWarnings("rawtypes")
    private EEnum getOrCreateEnumFromName(EPackage pkg, String qualifiedName) {
        String simpleName = simpleNameOf(qualifiedName);
        if (enumMap.containsKey(simpleName)) return enumMap.get(simpleName);

        EEnum eEnum = ef.createEEnum();
        eEnum.setName(simpleName);

        // Try to load the actual Java enum class to get literals
        try {
            Class<?> enumClass = Class.forName(qualifiedName);
            if (enumClass.isEnum()) {
                Object[] constants = enumClass.getEnumConstants();
                for (int i = 0; i < constants.length; i++) {
                    EEnumLiteral lit = ef.createEEnumLiteral();
                    lit.setName(((Enum) constants[i]).name());
                    lit.setValue(i);
                    lit.setLiteral(((Enum) constants[i]).name());
                    eEnum.getELiterals().add(lit);
                }
            }
        } catch (ClassNotFoundException e) {
            // Enum not on classpath; create with just the name
        }

        pkg.getEClassifiers().add(eEnum);
        enumMap.put(simpleName, eEnum);
        return eEnum;
    }

    private boolean hasSuperFeature(EClass ec, String featureName) {
        for (EClass superEc : ec.getESuperTypes()) {
            if (superEc.getEStructuralFeature(featureName) != null) {
                return true;
            }
        }
        return false;
    }

    private String simpleNameOf(String qualifiedName) {
        int dot = qualifiedName.lastIndexOf('.');
        return dot >= 0 ? qualifiedName.substring(dot + 1) : qualifiedName;
    }

    private void setMultiplicity(EStructuralFeature feature, ContainerKind kind) {
        switch (kind) {
            case SINGLE -> {
                feature.setLowerBound(0);
                feature.setUpperBound(1);
            }
            case LIST, SET, MAP -> {
                feature.setLowerBound(0);
                feature.setUpperBound(-1);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        SpoonEcoreGenerator gen = new SpoonEcoreGenerator();
        EPackage pkg = gen.generate();

        // Summary
        long classCount = pkg.getEClassifiers().stream().filter(c -> c instanceof EClass).count();
        long enumCount = pkg.getEClassifiers().stream().filter(c -> c instanceof EEnum).count();
        long featureCount = pkg.getEClassifiers().stream()
                .filter(c -> c instanceof EClass)
                .mapToLong(c -> ((EClass) c).getEStructuralFeatures().size())
                .sum();
        System.out.println("Generated spoon.ecore:");
        System.out.println("  EClasses: " + classCount);
        System.out.println("  EEnums:   " + enumCount);
        System.out.println("  Features: " + featureCount);

        // Save
        Path outputPath = args.length > 0
                ? Path.of(args[0])
                : Path.of("src/main/resources/metamodels/spoon.ecore");

        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("ecore", new EcoreResourceFactoryImpl());
        Resource resource = rs.createResource(URI.createFileURI(outputPath.toAbsolutePath().toString()));
        resource.getContents().add(pkg);
        resource.save(null);

        System.out.println("Saved to: " + outputPath.toAbsolutePath());
    }
}
