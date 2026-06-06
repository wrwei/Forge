package forge.transformations.m2m;

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
 * Loads the RoboChart state machine metamodel ({@code robochart.ecore}) dynamically
 * at runtime and provides typed lookups for EClasses, EEnums, and EEnumLiterals.
 *
 * <p>This is a self-contained subset of the official RoboChart metamodel
 * (nsURI {@code http://www.robocalc.circus/RoboChart}) covering the state machine
 * elements needed for the Java-to-RoboChart transformation.
 */
public final class RoboChartMetamodel {

    public static final String NS_URI = "http://www.robocalc.circus/RoboChart";
    private static final String ECORE_RESOURCE = "metamodels/robochart.ecore";

    private final EPackage ePackage;
    private final Map<String, EClassifier> classifierCache = new HashMap<>();

    private static RoboChartMetamodel instance;

    private RoboChartMetamodel() {
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

    public static synchronized RoboChartMetamodel getInstance() {
        if (instance == null) {
            instance = new RoboChartMetamodel();
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
        throw new IllegalArgumentException("No EClass named '" + name + "' in RoboChart metamodel");
    }

    public EEnum eEnum(String name) {
        EClassifier c = classifierCache.get(name);
        if (c instanceof EEnum ee) {
            return ee;
        }
        throw new IllegalArgumentException("No EEnum named '" + name + "' in RoboChart metamodel");
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
