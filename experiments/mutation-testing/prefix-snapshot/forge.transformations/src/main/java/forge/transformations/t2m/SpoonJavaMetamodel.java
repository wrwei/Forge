package forge.transformations.t2m;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads the generated Spoon Java metamodel ({@code spoon.ecore}) dynamically
 * at runtime and provides typed lookups for EClasses, EEnums, and EEnumLiterals.
 */
public final class SpoonJavaMetamodel {

    public static final String NS_URI = "http://spoon.gforge.inria.fr/spoon";
    private static final String ECORE_RESOURCE = "metamodels/spoon.ecore";

    private final EPackage ePackage;
    private final Map<String, EClassifier> classifierCache = new HashMap<>();

    private static SpoonJavaMetamodel instance;

    private SpoonJavaMetamodel() {
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("ecore", new EcoreResourceFactoryImpl());

        Resource ecoreResource = rs.createResource(URI.createURI(ECORE_RESOURCE));
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(ECORE_RESOURCE)) {
            if (is == null) {
                throw new IllegalStateException("Cannot find " + ECORE_RESOURCE + " on classpath");
            }
            ecoreResource.load(is, null);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + ECORE_RESOURCE, e);
        }

        ePackage = (EPackage) ecoreResource.getContents().get(0);
        EPackage.Registry.INSTANCE.put(NS_URI, ePackage);

        for (EClassifier classifier : ePackage.getEClassifiers()) {
            classifierCache.put(classifier.getName(), classifier);
        }
    }

    public static synchronized SpoonJavaMetamodel getInstance() {
        if (instance == null) {
            instance = new SpoonJavaMetamodel();
        }
        return instance;
    }

    public EPackage ePackage() {
        return ePackage;
    }

    public EClass eClass(String name) {
        EClassifier c = classifierCache.get(name);
        if (c instanceof EClass ec) {
            return ec;
        }
        throw new IllegalArgumentException("No EClass named '" + name + "' in Spoon metamodel");
    }

    public EEnum eEnum(String name) {
        EClassifier c = classifierCache.get(name);
        if (c instanceof EEnum ee) {
            return ee;
        }
        throw new IllegalArgumentException("No EEnum named '" + name + "' in Spoon metamodel");
    }

    public EEnumLiteral literal(String enumName, String literalName) {
        EEnum ee = eEnum(enumName);
        EEnumLiteral lit = ee.getEEnumLiteral(literalName);
        if (lit == null) {
            throw new IllegalArgumentException(
                    "No literal '" + literalName + "' in EEnum '" + enumName + "'");
        }
        return lit;
    }
}
