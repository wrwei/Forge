package forge.transformations.m2m;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import forge.transformations.t2m.SpoonDiscoverer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A sensor function called inside an ARGUMENT of a multi-argument actuator
 * call must be declared as a RoboChart Function.
 *
 * <p>The extractor turns {@code actuator.show(LEVEL, sensor.lampFor(side))} in
 * a transition's action into a RoboChart operation {@code Call}. The
 * function-name collector used to descend only into statement sequences and
 * assignments, so {@code lampFor} was used in the model but never declared, and
 * the RoboChart CSP generator stopped on an unresolved reference. The same
 * function read on the right-hand side of an assignment was always declared;
 * the second test pins that this path is unchanged.
 */
class M2mCallArgumentFunctionTest {

    @TempDir
    Path tempDir;

    private SpoonDiscoverer discoverer;
    private Java2RoboChartTransformer transformer;

    @BeforeEach
    void setUp() {
        discoverer = new SpoonDiscoverer();
        transformer = new Java2RoboChartTransformer();
    }

    @Test
    void functionCalledInsideAnOperationCallArgumentIsDeclared() throws IOException {
        writeFixture("actuator.show(CrossingConstants.LEVEL, sensor.lampFor(side));");
        EObject fn = functionNamed(rcPackage(), "lampFor");
        assertNotNull(fn, "lampFor is called inside the argument of an operation "
                + "call and must be declared as a RoboChart Function");
        assertEquals(1, getChildren(fn, "parameters").size(), "lampFor takes one parameter");
    }

    @Test
    void functionAssignedBeforeTheCallIsStillDeclared() throws IOException {
        writeFixture("lamp = sensor.lampFor(side);\n                actuator.show(CrossingConstants.LEVEL, lamp);");
        assertNotNull(functionNamed(rcPackage(), "lampFor"),
                "the assignment path declared lampFor before the fix and must still do so");
    }

    private void writeFixture(String entryAction) throws IOException {
        writeSource("crossing", "CrossingMode.java", """
                package crossing;
                public enum CrossingMode { WAITING, CROSSING }
                """);
        writeSource("crossing", "Side.java", """
                package crossing;
                public enum Side { NEAR, FAR }
                """);
        writeSource("crossing", "Lamp.java", """
                package crossing;
                public enum Lamp { GREEN, RED }
                """);
        writeSource("crossing", "InputEvent.java", """
                package crossing;
                public sealed interface InputEvent {
                    record ButtonPressed(Side side) implements InputEvent {}
                }
                """);
        writeSource("crossing", "CrossingConstants.java", """
                package crossing;
                public final class CrossingConstants {
                    public static final double LEVEL = 1.0;
                }
                """);
        writeSource("crossing", "CrossingSensor.java", """
                package crossing;
                public final class CrossingSensor {
                    public Lamp lampFor(Side s) {
                        if (s == Side.NEAR) { return Lamp.GREEN; }
                        return Lamp.RED;
                    }
                }
                """);
        writeSource("crossing", "Actuator.java", """
                package crossing;
                public final class Actuator {
                    public void show(double level, Lamp lamp) { }
                }
                """);
        writeSource("crossing", "CrossingController.java", """
                package crossing;
                public final class CrossingController {
                    private CrossingMode currentMode = CrossingMode.WAITING;
                    private final CrossingSensor sensor;
                    private final Actuator actuator;
                    private Side side = Side.NEAR;
                    private Lamp lamp = Lamp.RED;

                    public CrossingController(CrossingSensor sensor, Actuator actuator) {
                        this.sensor = sensor;
                        this.actuator = actuator;
                    }

                    public void step(InputEvent event) {
                        if (currentMode == CrossingMode.WAITING) {
                            if (event instanceof InputEvent.ButtonPressed) {
                                InputEvent.ButtonPressed bp = (InputEvent.ButtonPressed) event;
                                side = bp.side();
                                currentMode = CrossingMode.CROSSING;
                                %s
                            }
                        }
                    }
                }
                """.formatted(entryAction));
    }

    private EObject rcPackage() throws IOException {
        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, discoverer.getResolvedValues());
        return rcResource.getContents().get(0);
    }

    private static EObject functionNamed(EObject pkg, String name) {
        for (EObject f : getChildren(pkg, "functions")) {
            if (name.equals(get(f, "name"))) {
                return f;
            }
        }
        return null;
    }

    private void writeSource(String packageDir, String fileName, String source) throws IOException {
        Path dir = tempDir.resolve(packageDir);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(fileName), source);
    }

    private static Object get(EObject obj, String featureName) {
        EStructuralFeature f = obj.eClass().getEStructuralFeature(featureName);
        return f != null ? obj.eGet(f) : null;
    }

    @SuppressWarnings("unchecked")
    private static List<EObject> getChildren(EObject obj, String featureName) {
        Object val = get(obj, featureName);
        return val instanceof List<?> list ? (List<EObject>) list : List.of();
    }
}
