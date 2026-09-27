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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import forge.transformations.m2m.RoboChartMetamodel;

/**
 * Regression test for defect U2 (TOSEM revision, semantic-preservation audit
 * 2026-08-31): the {@code .rct} text renderer lost ALL arithmetic grouping.
 *
 * <p>{@code robochart2rct.egl}'s {@code formatRctExprPrec} handled only the two
 * boolean precedence levels ({@code /\} and {@code \/}); every other
 * {@code BinaryExpression} was emitted as bare {@code left op right}. The
 * RoboChart model was correct — the flat TEXT was not, and because the
 * standalone CSP generator RE-PARSES that text, a rendering bug became a wrong
 * model downstream. The witness was LRE {@code CalcCPA}: the emitted
 * {@code .rct} evaluated to a different value than {@code CalcCPA.java:40-47}
 * on every random input vector, and {@code CalcCPA.csp:214} applied
 * {@code Div} to {@code ew_vel} alone.
 *
 * <p>The property asserted here is the one the CSP generator depends on:
 * <b>the emitted text must re-parse to the SAME tree it was rendered from</b>.
 * Each case below states an input tree in prefix notation and the exact
 * expected emission. Without the fix, the parenthesised cases all emit flat
 * text and every assertion fails; {@code plusChainNeedsNoBrackets} guards the
 * other direction, so the fix cannot be trivially satisfied by wrapping
 * everything in parentheses.
 *
 * <p>Precedence levels are read off the RoboChart Xtext grammar
 * ({@code circus.robocalc.robochart.textual} 3.1.0): PlusMinus &lt; MultDivMod
 * &lt; CatExp &lt; Neg, all left-associative.
 */
class RctArithmeticGroupingRegressionTest {

    /** name -> {prefix-notation tree, exact expected .rct emission}. */
    private static Map<String, String[]> cases() {
        Map<String, String[]> c = new LinkedHashMap<>();
        // A looser-binding child under a tighter parent must be bracketed.
        c.put("multOverPlus",   new String[]{"(Mult (Plus a b) c)",       "(a + b) * c"});
        // Right operand of a non-associative operator: a - (b - c) is NOT
        // a - b - c. This is the class of error that made CalcCPA wrong.
        c.put("rightOfMinus",   new String[]{"(Minus a (Minus b c))",     "a - (b - c)"});
        // Right operand of Div at the same level must be bracketed too.
        c.put("rightOfDiv",     new String[]{"(Div a (Mult b c))",        "a / (b * c)"});
        // Unary minus: the grammar's Neg rule takes a Neg-level operand.
        c.put("negOverPlus",    new String[]{"(Neg (Plus a b))",          "-(a + b)"});
        c.put("divBothSides",   new String[]{"(Div (Plus a b) (Plus c d))", "(a + b) / (c + d)"});
        // The LRE CalcCPA tcpa shape, structurally identical to
        // CalcCPA.java:40-43.
        c.put("calcCpaShape",   new String[]{
                "(Div (Neg (Plus (Mult a (Minus b c)) (Mult d (Minus a b))))"
                + " (Plus (Mult (Minus b c) (Minus b c)) (Mult (Minus a b) (Minus a b))))",
                "-(a * (b - c) + d * (a - b)) / ((b - c) * (b - c) + (a - b) * (a - b))"});
        // GUARD (must NOT change): + is associative and left-nested, so the
        // left-recursive grammar rule accepts it unbracketed. A fix that just
        // parenthesises everything would emit "(a + b) + c" and fail here.
        c.put("plusChainNeedsNoBrackets",
                                new String[]{"(Plus (Plus a b) c)",       "a + b + c"});
        return c;
    }

    @Test
    void arithmeticEmissionPreservesGrouping(@TempDir Path tmp) throws Exception {
        Map<String, String[]> cases = cases();
        List<String> names = new ArrayList<>(cases.keySet());

        Path output = tmp.resolve("out");
        Files.createDirectories(output);
        Path xmi = output.resolve("robochart_model.xmi");
        Files.writeString(xmi, buildModel(names, cases));

        RoboChartMetamodel.getInstance();
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("xmi", new XMIResourceFactoryImpl());
        Resource res = rs.getResource(
                URI.createFileURI(xmi.toAbsolutePath().toString()), true);

        Path rct = output.resolve("robochart_controller.rct");
        new RoboChart2RctTransformer().transform(res, rct);
        assertTrue(Files.exists(rct), "the .rct should have been written");

        // Each expression is emitted as `action r<i> = <expr>` inside Op<i>.
        Map<String, String> emitted = new LinkedHashMap<>();
        for (String line : Files.readString(rct).split("\n")) {
            String s = line.trim();
            if (!s.startsWith("action r")) continue;
            int eq = s.indexOf(" = ");
            if (eq < 0) continue;
            int idx = Integer.parseInt(s.substring("action r".length(), eq));
            emitted.put(names.get(idx), s.substring(eq + 3));
        }

        assertEquals(names.size(), emitted.size(),
                "every witness expression should appear in the .rct");
        for (String name : names) {
            assertEquals(cases.get(name)[1], emitted.get(name),
                    "U2: wrong grouping emitted for case '" + name + "'");
        }
    }

    // ── Model construction ───────────────────────────────────────────────────
    // One operation per case; each has a single transition whose action assigns
    // the case's expression tree to a real-typed variable. Built as XMI text so
    // the test states the INPUT TREE explicitly rather than depending on the
    // T2M/M2M stages (whose own behaviour is covered elsewhere) to produce it.

    private static String buildModel(List<String> names, Map<String, String[]> cases) {
        StringBuilder ops = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            String rhs = xmiExpr(new Cursor(cases.get(names.get(i))[0]), "right", 12);
            ops.append("""
                <operations name="Op%1$d" terminates="true">
                  <variableList>
                    <vars name="r%1$d">
                      <type xsi:type="robochart:TypeRef" ref="/0/@types.0"/>
                    </vars>
                  </variableList>
                  <nodes xsi:type="robochart:Initial" name="i0"/>
                  <nodes xsi:type="robochart:Final" name="f0"/>
                  <transitions name="t0" source="/0/@operations.%1$d/@nodes.0" \
target="/0/@operations.%1$d/@nodes.1">
                    <action xsi:type="robochart:SeqStatement">
                      <statements xsi:type="robochart:Assignment">
                        <left xsi:type="robochart:VarRef" \
name="/0/@operations.%1$d/@variableList.0/@vars.0"/>
                    %2$s
                      </statements>
                    </action>
                  </transitions>
                </operations>
                """.formatted(i, rhs));
        }
        StringBuilder vars = new StringBuilder();
        for (String leaf : new String[]{"a", "b", "c", "d"}) {
            vars.append("        <vars name=\"").append(leaf)
                .append("\"><type xsi:type=\"robochart:TypeRef\" ref=\"/0/@types.0\"/></vars>\n");
        }
        // stripLeading() is load-bearing: the `\` line continuations below sit
        // at column 0, which zeroes the text block's incidental indentation, so
        // every other line keeps its 16-space prefix. Interior indentation is
        // harmless in XML, but an XML declaration must be the very first thing
        // in the document — with a space before `<?xml` the SAX parser rejects
        // it as a stray processing instruction.
        return """
                <?xml version="1.0" encoding="ASCII"?>
                <xmi:XMI xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" \
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
xmlns:robochart="http://www.robocalc.circus/RoboChart">
                  <robochart:RCPackage name="u2witness">
                    <interfaces name="Sensors">
                      <variableList>
                %s      </variableList>
                    </interfaces>
                    <interfaces name="Constants"/>
                    <types xsi:type="robochart:PrimitiveType" name="real"/>
                    <machines name="WitnessController">
                      <events name="tick"/>
                      <nodes xsi:type="robochart:State" name="S"/>
                      <nodes xsi:type="robochart:Initial" name="i0"/>
                      <transitions name="t_init" source="/0/@machines.0/@nodes.1" \
target="/0/@machines.0/@nodes.0"/>
                      <transitions name="t1" source="/0/@machines.0/@nodes.0" \
target="/0/@machines.0/@nodes.0">
                        <trigger event="/0/@machines.0/@events.0"/>
                      </transitions>
                    </machines>
                %s  </robochart:RCPackage>
                </xmi:XMI>
                """.formatted(vars, ops).stripLeading();
    }

    /** Tokenising cursor over the prefix notation, e.g. {@code (Mult (Plus a b) c)}. */
    private static final class Cursor {
        private final String s;
        private int i;
        Cursor(String s) { this.s = s; }
        void ws() { while (i < s.length() && s.charAt(i) == ' ') i++; }
        char peek() { ws(); return i < s.length() ? s.charAt(i) : '\0'; }
        void expect(char c) {
            ws();
            if (i >= s.length() || s.charAt(i) != c) {
                throw new IllegalStateException("expected '" + c + "' at " + i + " in " + s);
            }
            i++;
        }
        String word() {
            ws();
            int st = i;
            while (i < s.length() && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '.')) i++;
            if (st == i) throw new IllegalStateException("expected word at " + i + " in " + s);
            return s.substring(st, i);
        }
    }

    /** Render one prefix-notation node as RoboChart XMI, tagged {@code tag}. */
    private static String xmiExpr(Cursor c, String tag, int indent) {
        String pad = " ".repeat(indent);
        if (c.peek() != '(') {
            String w = c.word();
            if (Character.isDigit(w.charAt(0))) {
                String cls = w.contains(".") ? "FloatExp" : "IntegerExp";
                return pad + "<" + tag + " xsi:type=\"robochart:" + cls
                        + "\" value=\"" + w + "\"/>";
            }
            // A bare name is a nullary CallExp — the shape the M2M produces for
            // a sensor read such as `sensor.ns_vel()`.
            return pad + "<" + tag + " xsi:type=\"robochart:CallExp\">\n"
                 + pad + "  <function xsi:type=\"robochart:StringExp\" value=\"" + w + "\"/>\n"
                 + pad + "</" + tag + ">";
        }
        c.expect('(');
        String op = c.word();
        String body;
        if ("Neg".equals(op) || "Not".equals(op)) {
            body = xmiExpr(c, "exp", indent + 2);
        } else {
            body = xmiExpr(c, "left", indent + 2) + "\n" + xmiExpr(c, "right", indent + 2);
        }
        c.expect(')');
        return pad + "<" + tag + " xsi:type=\"robochart:" + op + "\">\n"
             + body + "\n" + pad + "</" + tag + ">";
    }
}
