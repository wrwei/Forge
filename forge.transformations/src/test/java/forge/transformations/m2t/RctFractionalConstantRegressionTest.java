package forge.transformations.m2t;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import forge.transformations.m2m.RoboChartMetamodel;

/**
 * Regression test for defect U4 (TOSEM revision, semantic-preservation audit
 * 2026-08-31): fractional constants were SILENTLY ceiled, doubling a safety
 * threshold.
 *
 * <p>{@code SRangerConstants.java:20} declares
 * {@code obstacleThreshold = 0.5}; {@code constant_defaults.json} correctly
 * carried {@code "0.5"}; the emitted {@code .rct} read
 * {@code const obstaclethreshold : real = 1}, so the guard
 * {@code distance <= obstaclethreshold} fired on distances in (0.5, 1] that the
 * Java rejects — a 26% guard-disagreement rate over distances in [0, 2]. No
 * diagnostic of any kind was produced; the identical ceiling on variable
 * INITIALS was at least logged.
 *
 * <p>The workaround itself is genuinely required for the CSP target and was
 * verified against the generator rather than trusted: feeding
 * {@code = 0.5} to {@code circus.robocalc.robochart.generator.csp} 3.0.0 aborts
 * with {@code Case not treated by expression compiler: FloatExpImpl} (exit 255)
 * and emits no controller CSP at all. So the fix does not remove the ceiling —
 * it (a) makes every application of it a first-class pipeline warning, and
 * (b) gates it on the target so a consumer that accepts FloatExp gets the true
 * value.
 *
 * <p>Three assertions, each of which fails on the pre-fix template:
 * the ceiling is REPORTED; {@code float_constants=exact} carries the true
 * value; and an integral real (5.0 -> 5) is still emitted without a spurious
 * warning, since that rendering is exact rather than a workaround.
 */
class RctFractionalConstantRegressionTest {

    private static final String CEILED_CODE = "U4_FRACTIONAL_CONSTANT_CEILED";

    private Resource load(Path tmp) throws Exception {
        Path output = tmp.resolve("out");
        Files.createDirectories(output);
        Path xmi = output.resolve("robochart_model.xmi");
        Files.writeString(xmi, model());
        RoboChartMetamodel.getInstance();
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("xmi", new XMIResourceFactoryImpl());
        return rs.getResource(URI.createFileURI(xmi.toAbsolutePath().toString()), true);
    }

    private static Map<String, Object> defaults() {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("obstaclethreshold", "0.5");   // the SRanger witness
        d.put("movevel", "1");               // already integral
        d.put("integralreal", "5.0");        // integral-valued real
        return d;
    }

    /** Extract {@code name -> rendered value} from the {@code interface Constants} block. */
    private static Map<String, String> constants(Path rct) throws Exception {
        Map<String, String> out = new LinkedHashMap<>();
        boolean in = false;
        for (String line : Files.readString(rct).split("\n")) {
            String s = line.trim();
            if (s.startsWith("interface Constants")) { in = true; continue; }
            if (in && s.equals("}")) break;
            if (in && s.startsWith("const ")) {
                int eq = s.indexOf(" = ");
                if (eq < 0) continue;
                String name = s.substring("const ".length(), s.indexOf(" :")).trim();
                out.put(name, s.substring(eq + 3).trim());
            }
        }
        return out;
    }

    @Test
    void ceilingAFractionalConstantIsReported(@TempDir Path tmp) throws Exception {
        Resource res = load(tmp);
        Path rct = tmp.resolve("out/robochart_controller.rct");

        RoboChart2RctTransformer t = new RoboChart2RctTransformer();
        t.setConstantDefaults(defaults());
        // default mode: the CSP-safe workaround
        t.transform(res, rct);

        Map<String, String> consts = constants(rct);
        assertEquals("1", consts.get("obstaclethreshold"),
                "the CSP-target workaround should still ceil 0.5 to 1");

        List<Map<String, String>> warnings = t.getWarnings();
        // THE defect: before the fix this list did not exist and the ceiling
        // produced no output whatsoever.
        assertTrue(warnings.stream().anyMatch(w -> CEILED_CODE.equals(w.get("code"))),
                "U4: ceiling a fractional constant must emit a "
                        + CEILED_CODE + " warning, got: " + warnings);
        Map<String, String> w = warnings.stream()
                .filter(x -> CEILED_CODE.equals(x.get("code"))).findFirst().orElseThrow();
        assertTrue(w.get("message").contains("obstaclethreshold")
                        && w.get("message").contains("0.5"),
                "the warning should name the constant and its true value: " + w.get("message"));
    }

    @Test
    void exactModeCarriesTheTrueValue(@TempDir Path tmp) throws Exception {
        Resource res = load(tmp);
        Path rct = tmp.resolve("out/robochart_controller.rct");

        RoboChart2RctTransformer t = new RoboChart2RctTransformer();
        t.setConstantDefaults(defaults());
        t.setFloatConstantMode("exact");
        t.transform(res, rct);

        assertEquals("0.5", constants(rct).get("obstaclethreshold"),
                "U4: float_constants=exact must emit the Java value, not the ceiling");
        assertFalse(t.getWarnings().stream().anyMatch(x -> CEILED_CODE.equals(x.get("code"))),
                "no ceiling happened, so no ceiling warning should be reported");
    }

    @Test
    void integralRealNeedsNoWorkaroundAndNoWarning(@TempDir Path tmp) throws Exception {
        Resource res = load(tmp);
        Path rct = tmp.resolve("out/robochart_controller.rct");

        RoboChart2RctTransformer t = new RoboChart2RctTransformer();
        t.setConstantDefaults(defaults());
        t.transform(res, rct);

        Map<String, String> consts = constants(rct);
        // 5.0 -> "5" is an exact rendering (the CSP generator cannot take the
        // ".0" form, but no value is lost), so it must not be reported.
        assertEquals("5", consts.get("integralreal"),
                "an integral real should be emitted in integer form");
        assertEquals("1", consts.get("movevel"),
                "an already-integral default should pass through unchanged");
        assertEquals(1, t.getWarnings().stream()
                        .filter(x -> CEILED_CODE.equals(x.get("code"))).count(),
                "exactly one constant (0.5) is genuinely fractional, so exactly one "
                        + "warning is expected — an integral real must not warn: "
                        + t.getWarnings());
    }

    /**
     * Minimal model whose transition guard references all three constant names.
     *
     * <p>The EGL derives {@code constantEntries} from the names appearing in
     * guards (a name with an injected default is classified as a constant), NOT
     * from an {@code interface Constants} declaration in the model — so the
     * witness names have to appear in a guard to reach the emission site under
     * test. The guard is
     * {@code distance <= obstaclethreshold /\ distance <= movevel
     * /\ distance <= integralreal}, mirroring the shape of SRanger's t2.
     */
    private static String model() {
        // See RctArithmeticGroupingRegressionTest for why stripLeading() is
        // needed: the `\` continuations at column 0 zero the incidental
        // indentation, and `<?xml` must start the document.
        return """
                <?xml version="1.0" encoding="ASCII"?>
                <xmi:XMI xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" \
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
xmlns:robochart="http://www.robocalc.circus/RoboChart">
                  <robochart:RCPackage name="u4witness">
                    <interfaces name="Sensors">
                      <variableList>
                        <vars name="distance">
                          <type xsi:type="robochart:TypeRef" ref="/0/@types.0"/>
                        </vars>
                      </variableList>
                    </interfaces>
                    <interfaces name="Constants"/>
                    <types xsi:type="robochart:PrimitiveType" name="real"/>
                    <machines name="WitnessController">
                      <events name="tick"/>
                      <nodes xsi:type="robochart:State" name="Moving"/>
                      <nodes xsi:type="robochart:Initial" name="i0"/>
                      <transitions name="t_init" source="/0/@machines.0/@nodes.1" \
target="/0/@machines.0/@nodes.0"/>
                      <transitions name="t1" source="/0/@machines.0/@nodes.0" \
target="/0/@machines.0/@nodes.0">
                        <trigger event="/0/@machines.0/@events.0"/>
                        <condition xsi:type="robochart:And">
                          <left xsi:type="robochart:LessOrEqual">
                            <left xsi:type="robochart:CallExp">
                              <function xsi:type="robochart:StringExp" value="distance"/>
                            </left>
                            <right xsi:type="robochart:CallExp">
                              <function xsi:type="robochart:StringExp" value="obstaclethreshold"/>
                            </right>
                          </left>
                          <right xsi:type="robochart:And">
                            <left xsi:type="robochart:LessOrEqual">
                              <left xsi:type="robochart:CallExp">
                                <function xsi:type="robochart:StringExp" value="distance"/>
                              </left>
                              <right xsi:type="robochart:CallExp">
                                <function xsi:type="robochart:StringExp" value="movevel"/>
                              </right>
                            </left>
                            <right xsi:type="robochart:LessOrEqual">
                              <left xsi:type="robochart:CallExp">
                                <function xsi:type="robochart:StringExp" value="distance"/>
                              </left>
                              <right xsi:type="robochart:CallExp">
                                <function xsi:type="robochart:StringExp" value="integralreal"/>
                              </right>
                            </right>
                          </right>
                        </condition>
                      </transitions>
                    </machines>
                  </robochart:RCPackage>
                </xmi:XMI>
                """.stripLeading();
    }
}
