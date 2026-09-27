package forge.transformations.t2m;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtField;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for the T2M-2 fix (negative-literal sign loss) and the
 * T2M-1 fix (self-contained literal values in the persisted model).
 *
 * Background: Spoon parses {@code -1} as {@code CtUnaryOperator(NEG, CtLiteral(1))}.
 * Before the fix, ValueResolver keyed the INNER literal with value +1 and gave the
 * unary node no entry, so initialiser lookups missed (falling back to type
 * defaults downstream) or saw the wrong sign — the mechanism behind the
 * LRE {@code cstc = -1} → Dafny {@code cstc := 0} divergence.
 */
class ValueResolverNegativeLiteralTest {

    private static final String SENTINEL_SRC = """
            package x;
            public class C {
                static final int SENTINEL = -1;
                static final double NEG_D = -2.5;
                static final int PLAIN = 7;
                private int cstc = -1;
                private double tcpa = -1.0;
            }
            """;

    private CtModel parse(Path tmp) throws Exception {
        Path src = tmp.resolve("src");
        Files.createDirectories(src);
        Files.writeString(src.resolve("C.java"), SENTINEL_SRC);
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setComplianceLevel(17);
        launcher.getEnvironment().setShouldCompile(false);
        launcher.addInputResource(src.toAbsolutePath().toString());
        launcher.buildModel();
        return launcher.getModel();
    }

    @Test
    void constantMapFoldsNegatedLiterals(@TempDir Path tmp) throws Exception {
        Map<String, Object> constants = ValueResolver.buildConstantMap(parse(tmp));
        assertEquals(-1, constants.get("C.SENTINEL"),
                "negated int constant must resolve with its sign");
        assertEquals(-2.5, constants.get("C.NEG_D"),
                "negated double constant must resolve with its sign");
        assertEquals(7, constants.get("C.PLAIN"),
                "plain literal path must be unaffected");
        assertEquals(-1, constants.get("SENTINEL"),
                "unqualified fallback key must also carry the sign");
    }

    @Test
    void resolveKeysNegatedValueOnUnaryNode(@TempDir Path tmp) throws Exception {
        CtModel model = parse(tmp);
        SpoonDiscoverer discoverer = new SpoonDiscoverer();
        Path srcRoot = tmp.resolve("src");
        discoverer.discover(srcRoot);
        Map<EObject, Object> resolved = discoverer.getResolvedValues();

        // Find the EMF object for the field initialiser unary of cstc.
        // Re-discover gives us the mapping; assert that SOME EObject carries -1
        // (the unary node's entry) — before the fix, no entry held a negative value.
        boolean hasMinusOne = resolved.values().stream()
                .anyMatch(v -> v instanceof Integer i && i == -1);
        boolean hasMinusOneD = resolved.values().stream()
                .anyMatch(v -> v instanceof Double d && d == -1.0);
        assertTrue(hasMinusOne, "resolvedValues must contain -1 for `cstc = -1`");
        assertTrue(hasMinusOneD, "resolvedValues must contain -1.0 for `tcpa = -1.0`");
    }

    @Test
    void unaryNodesOfNegatedFieldInitialisersExist(@TempDir Path tmp) throws Exception {
        // Sanity: the shape we claim (CtUnaryOperator NEG wrapping the literal)
        // is really what Spoon produces for the test source.
        CtModel model = parse(tmp);
        long negUnaries = model.getElements(e -> e instanceof CtUnaryOperator<?> u
                && u.getKind() == spoon.reflect.code.UnaryOperatorKind.NEG).size();
        assertTrue(negUnaries >= 4, "expected NEG unaries for the four negative initialisers");
        CtField<?> sentinel = model.getElements(e -> e instanceof CtField<?> f
                && f.getSimpleName().equals("SENTINEL")).stream()
                .map(e -> (CtField<?>) e).findFirst().orElseThrow();
        assertTrue(sentinel.getDefaultExpression() instanceof CtUnaryOperator<?>,
                "SENTINEL initialiser must be a unary NEG node, not a literal");
    }

    @Test
    void persistedLiteralsCarryValues(@TempDir Path tmp) throws Exception {
        // T2M-1: the persisted XMI must be self-contained — literals carry their
        // value as a string attribute rather than only existing in resolvedValues.
        parse(tmp); // writes the source tree
        SpoonDiscoverer discoverer = new SpoonDiscoverer();
        var resource = discoverer.discover(tmp.resolve("src"));
        boolean anyValue = false;
        for (var it = resource.getAllContents(); it.hasNext(); ) {
            EObject obj = it.next();
            if (!obj.eClass().getName().equals("CtLiteral")) continue;
            var f = obj.eClass().getEStructuralFeature("value");
            assertNotNull(f, "CtLiteral EClass must have a `value` attribute");
            if (obj.eGet(f) != null) anyValue = true;
        }
        assertTrue(anyValue, "at least one persisted literal must carry a value");
    }

}