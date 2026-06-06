package forge.transformations.m2m;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;
import forge.transformations.t2m.SpoonDiscoverer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import forge.transformations.core.Phase;

/**
 * Tests for {@link Java2RoboChartTransformer} extracting RoboChart state machines
 * from MoDisco Java models containing flat if-else state machine patterns.
 */
class Java2RoboChartTransformTest {

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
    void extractsStatesFromModeEnum(TestInfo testInfo) throws IOException {
        writeMinimalStateMachine();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);

        // Should have 3 states (S1, S2, S3) + Initial node
        List<EObject> nodes = getChildren(stm, "nodes");
        List<EObject> states = nodes.stream()
                .filter(n -> n.eClass().getName().equals("State"))
                .toList();
        List<EObject> initials = nodes.stream()
                .filter(n -> n.eClass().getName().equals("Initial"))
                .toList();

        assertEquals(3, states.size(), "Expected 3 states");
        assertEquals(1, initials.size(), "Expected 1 initial junction");

        List<String> stateNames = states.stream()
                .map(s -> (String) get(s, "name"))
                .toList();
        assertTrue(stateNames.contains("S1"));
        assertTrue(stateNames.contains("S2"));
        assertTrue(stateNames.contains("S3"));
    }

    @Test
    void extractsTransitionsFromIfElse(TestInfo testInfo) throws IOException {
        writeMinimalStateMachine();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);

        List<EObject> transitions = getChildren(stm, "transitions");

        // At least: t_init + transitions from the if-else chain
        assertTrue(transitions.size() >= 2,
                "Expected at least 2 transitions (init + at least 1 from if-else), got " + transitions.size());

        // Check that the initial transition exists
        boolean hasInitTransition = transitions.stream()
                .anyMatch(t -> "t_init".equals(get(t, "name")));
        assertTrue(hasInitTransition, "Expected initial transition t_init");
    }

    @Test
    void extractsStateMachineName(TestInfo testInfo) throws IOException {
        writeMinimalStateMachine();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);

        assertEquals("MyController", get(stm, "name"));
    }

    @Test
    void extractsEventsFromInstanceofChecks(TestInfo testInfo) throws IOException {
        writeMinimalStateMachine();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);

        List<EObject> events = getChildren(stm, "events");
        assertTrue(events.size() >= 1,
                "Expected at least 1 event, got " + events.size());
    }

    @Test
    void handlesEmptyModel(TestInfo testInfo) throws IOException {
        writeSource("com/example", "Empty.java", """
                package com.example;
                public class Empty {}
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        // Should produce a valid but empty RoboChart package
        EObject rcPackage = rcResource.getContents().get(0);
        assertNotNull(rcPackage);
        List<EObject> machines = getChildren(rcPackage, "machines");
        assertEquals(0, machines.size(), "No state machine expected for non-controller code");
    }

    @Test
    void extractsTransitionsFromModeNestedIfElse(TestInfo testInfo) throws IOException {
        writeModeNestedStateMachine();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);

        // States: S1, S2, S3 + Initial
        List<EObject> nodes = getChildren(stm, "nodes");
        List<EObject> states = nodes.stream()
                .filter(n -> n.eClass().getName().equals("State"))
                .toList();
        assertEquals(3, states.size(), "Expected 3 states");

        // Transitions: t_init + t1 (S1->S2, GoS2) + t2 (S1->S3, GoS3) + t3 (S2->S1, no trigger)
        List<EObject> transitions = getChildren(stm, "transitions");
        assertEquals(4, transitions.size(),
                "Expected 4 transitions (t_init + 3 from mode-nested if-else), got " + transitions.size());

        // Events: goS2, goS3 (camelCase per ETL convention — Java record GoS2 -> event goS2)
        List<EObject> events = getChildren(stm, "events");
        List<String> eventNames = events.stream()
                .map(e -> (String) get(e, "name"))
                .toList();
        assertEquals(2, eventNames.size(), "Expected 2 events");
        assertTrue(eventNames.contains("goS2"));
        assertTrue(eventNames.contains("goS3"));

        // Verify transition details (skip t_init at index 0)
        // t1: S1 -> S2, trigger goS2 (from inner chain of S1 block)
        EObject t1 = transitions.get(1);
        assertEquals("t1", get(t1, "name"));
        assertEquals("S1", get((EObject) get(t1, "source"), "name"));
        assertEquals("S2", get((EObject) get(t1, "target"), "name"));
        EObject t1Trigger = (EObject) get(t1, "trigger");
        assertNotNull(t1Trigger, "t1 should have a trigger");
        assertEquals("goS2", get((EObject) get(t1Trigger, "event"), "name"));

        // t2: S1 -> S3, trigger goS3
        EObject t2 = transitions.get(2);
        assertEquals("t2", get(t2, "name"));
        assertEquals("S1", get((EObject) get(t2, "source"), "name"));
        assertEquals("S3", get((EObject) get(t2, "target"), "name"));
        EObject t2Trigger = (EObject) get(t2, "trigger");
        assertNotNull(t2Trigger, "t2 should have a trigger");
        assertEquals("goS3", get((EObject) get(t2Trigger, "event"), "name"));

        // t3: S2 -> S1, no trigger (direct assignment in S2 mode block)
        EObject t3 = transitions.get(3);
        assertEquals("t3", get(t3, "name"));
        assertEquals("S2", get((EObject) get(t3, "source"), "name"));
        assertEquals("S1", get((EObject) get(t3, "target"), "name"));
    }

    @Test
    void extractsTransitionsWithNamedPredicatesAndNesting(TestInfo testInfo) throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2, S3 }
                """);

        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                    record GoS3() implements MyEvent {}
                }
                """);

        // Named boolean predicate before if-else chain + mode-nested structure
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;

                    public void step(MyEvent event) {
                        boolean ready = isReady();

                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2 && ready) {
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }

                    private boolean isReady() { return true; }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);

        // Should not crash on CtLocalVariable before CtIf
        List<EObject> nodes = getChildren(stm, "nodes");
        List<EObject> states = nodes.stream()
                .filter(n -> n.eClass().getName().equals("State"))
                .toList();
        assertEquals(3, states.size(), "Expected 3 states");

        // Transitions: t_init + t1 (S1->S2, GoS2, guarded) + t2 (S2->S1)
        List<EObject> transitions = getChildren(stm, "transitions");
        assertEquals(3, transitions.size(),
                "Expected 3 transitions (t_init + 2), got " + transitions.size());

        // t1: S1 -> S2, trigger GoS2, guard should be CallExp("ready") from named predicate
        EObject t1 = transitions.get(1);
        assertEquals("S1", get((EObject) get(t1, "source"), "name"));
        assertEquals("S2", get((EObject) get(t1, "target"), "name"));
        EObject t1Trigger = (EObject) get(t1, "trigger");
        assertNotNull(t1Trigger, "t1 should have trigger GoS2");
        EObject condition = (EObject) get(t1, "condition");
        assertNotNull(condition, "t1 should have a guard condition from named predicate");
        assertEquals("CallExp", condition.eClass().getName(),
                "Guard should be a CallExp (named variable)");
        assertEquals("ready", getCallExpName(condition),
                "Guard should use variable name 'ready', not synthetic guard_tN");
    }

    @Test
    void extractsNegatedNamedPredicate(TestInfo testInfo) throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        boolean inDanger = checkDanger();
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2 && !inDanger) {
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                    private boolean checkDanger() { return false; }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t1: S1 -> S2, condition should be Not(exp=CallExp("inDanger"))
        EObject t1 = transitions.get(1);
        EObject condition = (EObject) get(t1, "condition");
        assertNotNull(condition, "t1 should have a guard condition");
        assertEquals("Not", condition.eClass().getName(),
                "Guard should be Not for negated predicate");
        EObject operand = (EObject) get(condition, "exp");
        assertNotNull(operand, "Not should have exp");
        assertEquals("CallExp", operand.eClass().getName());
        assertEquals("inDanger", getCallExpName(operand),
                "Operand should reference 'inDanger' variable name");
    }

    @Test
    void extractsMixedNamedAndUnnamedGuards(TestInfo testInfo) throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    private boolean armed = false;
                    public void step(MyEvent event) {
                        boolean ready = isReady();
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2 && ready && armed) {
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                    private boolean isReady() { return true; }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t1: S1 -> S2, two guard conjuncts: ready (CtVariableRead -> named)
        // and armed (CtFieldRead -> synthetic guard_t1_2)
        EObject t1 = transitions.get(1);
        EObject condition = (EObject) get(t1, "condition");
        assertNotNull(condition, "t1 should have a guard condition");
        assertEquals("And", condition.eClass().getName(),
                "Two conjuncts should produce And");

        // Left should be CallExp("ready") — named from local variable
        EObject left = (EObject) get(condition, "left");
        assertEquals("CallExp", left.eClass().getName());
        assertEquals("ready", getCallExpName(left),
                "Left conjunct should be named 'ready' from local variable");

        // Right should be CallExp("armed") — E1 fix: field reads now use actual name
        EObject right = (EObject) get(condition, "right");
        assertEquals("CallExp", right.eClass().getName());
        assertEquals("armed", getCallExpName(right),
                "Right conjunct should use field name 'armed' (E1 fix: no more opaque guard_tN)");
    }

    // === E6: Branch priority encoding in inner if-else chains ===

    @Test
    void innerChainEncodesBranchPriority(TestInfo testInfo) throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2, S3 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        // Two triggerless guarded branches from same mode: guardA then guardB
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        boolean guardA = checkA();
                        boolean guardB = checkB();
                        if (currentMode == MyMode.S1) {
                            if (guardA) {
                                currentMode = MyMode.S2;
                            } else if (guardB) {
                                currentMode = MyMode.S3;
                            }
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                    private boolean checkA() { return true; }
                    private boolean checkB() { return false; }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t_init, t1 (S1->S2, guardA), t2 (S1->S3, guardB), t3 (S2->S1)
        // The ETL deliberately does NOT encode else-if branch priority via
        // negated predecessors (walkInnerChain comment: "Priority encoding
        // removed (Phase 3): guard-only transitions are nondeterministic in
        // RoboChart when multiple guards are simultaneously true"). FDR4
        // nondeterminism from overlapping guards is documented as EXPECTED
        // in forge.assets/prompts/fdr4_system.txt.
        assertEquals(4, transitions.size(),
                "Expected 4 transitions (t_init + 3), got " + transitions.size());

        // t1: first branch — condition is bare CallExp("guardA")
        EObject t1 = transitions.get(1);
        assertEquals("S1", get((EObject) get(t1, "source"), "name"));
        assertEquals("S2", get((EObject) get(t1, "target"), "name"));
        EObject t1Cond = (EObject) get(t1, "condition");
        assertNotNull(t1Cond, "t1 should have guard condition");
        assertEquals("CallExp", t1Cond.eClass().getName(),
                "First branch guard should be simple CallExp");
        assertEquals("guardA", getCallExpName(t1Cond));

        // t2: second branch — condition is bare CallExp("guardB") (NOT
        // And(Not(guardA), guardB) — no priority encoding).
        EObject t2 = transitions.get(2);
        assertEquals("S1", get((EObject) get(t2, "source"), "name"));
        assertEquals("S3", get((EObject) get(t2, "target"), "name"));
        EObject t2Cond = (EObject) get(t2, "condition");
        assertNotNull(t2Cond, "t2 should have guard condition");
        assertEquals("CallExp", t2Cond.eClass().getName(),
                "Second branch guard should be bare CallExp (no priority encoding)");
        assertEquals("guardB", getCallExpName(t2Cond));
    }

    @Test
    void innerChainTerminalElseGetsPriority(TestInfo testInfo) throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2, S3 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        // Triggerless guard + terminal else
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        boolean guardA = checkA();
                        if (currentMode == MyMode.S1) {
                            if (guardA) {
                                currentMode = MyMode.S2;
                            } else {
                                currentMode = MyMode.S3;
                            }
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                    private boolean checkA() { return true; }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t_init, t1 (S1->S2, guardA), t2 (S1->S3, no condition), t3 (S2->S1)
        // The ETL deliberately does NOT encode else-if branch priority via
        // negated predecessors; terminal-else transitions get NO condition
        // (fire unconditionally when no other branch is taken). See the
        // walkInnerChain comment "Priority encoding removed (Phase 3)".
        // NOTE: the unconditional terminal-else τ-self-loop competes with
        // enabled guarded autonomous transitions in tock-CSP and causes
        // FDR4 :[deterministic] failures (see CLAUDE.md "Linter output"
        // section). Prefer event-triggered bare-precondition branches over
        // terminal-else fallbacks in real controllers.
        assertEquals(4, transitions.size(),
                "Expected 4 transitions (t_init + 3), got " + transitions.size());

        // t1: CallExp("guardA")
        EObject t1 = transitions.get(1);
        EObject t1Cond = (EObject) get(t1, "condition");
        assertNotNull(t1Cond, "t1 should have guard");
        assertEquals("CallExp", t1Cond.eClass().getName());
        assertEquals("guardA", getCallExpName(t1Cond));

        // t2: terminal else — no condition (unconditional).
        EObject t2 = transitions.get(2);
        assertEquals("S3", get((EObject) get(t2, "target"), "name"));
        EObject t2Cond = (EObject) get(t2, "condition");
        assertNull(t2Cond,
                "Terminal else should have no condition (no priority encoding)");
    }

    @Test
    void innerChainTriggeredBranchesNoFalsePriority(TestInfo testInfo) throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2, S3 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        // Event-triggered branch followed by triggerless guarded branch
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        boolean guardA = checkA();
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2) {
                                currentMode = MyMode.S2;
                            } else if (guardA) {
                                currentMode = MyMode.S3;
                            }
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                    private boolean checkA() { return true; }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t_init, t1 (S1->S2, GoS2, no guard), t2 (S1->S3, guardA — no negation), t3 (S2->S1)
        assertEquals(4, transitions.size(),
                "Expected 4 transitions, got " + transitions.size());

        // t1: event-triggered, no guard
        EObject t1 = transitions.get(1);
        assertNotNull(get(t1, "trigger"), "t1 should have trigger GoS2");
        assertNull(get(t1, "condition"), "t1 should have no guard (event-only)");

        // t2: triggerless with guard — should NOT have negated predecessor from event branch
        EObject t2 = transitions.get(2);
        assertNull(get(t2, "trigger"), "t2 should be triggerless");
        EObject t2Cond = (EObject) get(t2, "condition");
        assertNotNull(t2Cond, "t2 should have guard condition");
        assertEquals("CallExp", t2Cond.eClass().getName(),
                "Guard should be simple CallExp, not wrapped with negated predecessor");
        assertEquals("guardA", getCallExpName(t2Cond),
                "Guard should just be 'guardA' — event branch does not contribute predecessors");
    }

    // === E4: Action extraction from then-blocks ===

    @Test
    void thenBlockMethodCallExtractedAsAction(TestInfo testInfo) throws IOException {
        writeStateMachineWithSingleAction();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t_init, t1 (S1->S2, GoS2, action=doSomething), t2 (S2->S1)
        assertEquals(3, transitions.size(),
                "Expected 3 transitions (t_init + 2), got " + transitions.size());

        // t1: should have action = CommunicationStmt wrapping Communication(event="doSomething")
        EObject t1 = transitions.get(1);
        assertEquals("S2", get((EObject) get(t1, "target"), "name"));
        EObject action = (EObject) get(t1, "action");
        assertNotNull(action, "t1 should have an action from the method call");
        assertEquals("CommunicationStmt", action.eClass().getName(),
                "Single method call should produce CommunicationStmt");
        EObject comm = unwrapComm(action);
        EObject actionEvent = (EObject) get(comm, "event");
        assertNotNull(actionEvent, "Communication should have event");
        assertEquals("doSomething", get(actionEvent, "name"),
                "Action event should be named after the method");

        // Action event should also appear in the machine's events
        List<EObject> events = getChildren(stm, "events");
        List<String> eventNames = events.stream()
                .map(e -> (String) get(e, "name"))
                .toList();
        assertTrue(eventNames.contains("doSomething"),
                "Action event 'doSomething' should be in machine events");
    }

    @Test
    void multipleActionsExtractedAsSeqStatement(TestInfo testInfo) throws IOException {
        writeStateMachineWithMultipleActions();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t1: should have action = SeqStatement with 2 Communications
        EObject t1 = transitions.get(1);
        EObject action = (EObject) get(t1, "action");
        assertNotNull(action, "t1 should have an action");
        assertEquals("SeqStatement", action.eClass().getName(),
                "Multiple method calls should produce SeqStatement");

        List<EObject> stmts = getChildren(action, "statements");
        assertEquals(2, stmts.size(), "SeqStatement should have 2 statements");

        assertEquals("CommunicationStmt", stmts.get(0).eClass().getName());
        assertEquals("doFirst", get((EObject) get(unwrapComm(stmts.get(0)), "event"), "name"));
        assertEquals("CommunicationStmt", stmts.get(1).eClass().getName());
        assertEquals("doSecond", get((EObject) get(unwrapComm(stmts.get(1)), "event"), "name"));
    }

    @Test
    void modeAssignmentNotIncludedInActions(TestInfo testInfo) throws IOException {
        writeMinimalStateMachine();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // All transitions should have no action (only mode assignments in then-blocks)
        for (int i = 1; i < transitions.size(); i++) {
            EObject t = transitions.get(i);
            assertNull(get(t, "action"),
                    "Transition " + get(t, "name") + " should have no action (mode-only then-block)");
        }
    }

    // === E11: Event payload / pattern variable data flow ===

    @Test
    void patternVariableMarksEventAsTyped(TestInfo testInfo) throws IOException {
        writeStateMachineWithEventPayload();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> events = getChildren(stm, "events");

        // goS2 (used with cast variable rv) should have type set
        EObject goS2 = events.stream()
                .filter(e -> "goS2".equals(get(e, "name")))
                .findFirst().orElseThrow();
        assertNotNull(get(goS2, "type"),
                "goS2 event should have type set (cast variable present)");
    }

    @Test
    void plainInstanceofKeepsEventUntyped(TestInfo testInfo) throws IOException {
        writeStateMachineWithEventPayload();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> events = getChildren(stm, "events");

        // goS1 (used without cast variable) should have no type
        EObject goS1 = events.stream()
                .filter(e -> "goS1".equals(get(e, "name")))
                .findFirst().orElseThrow();
        assertNull(get(goS1, "type"),
                "goS1 event should have no type (plain instanceof, no cast variable)");
    }

    @Test
    void actionCommunicationHasValueFromPatternVariable(TestInfo testInfo) throws IOException {
        writeStateMachineWithEventPayload();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t1: S1 -> S2 with trigger GoS2 and action doAction(rv.value())
        EObject t1 = transitions.get(1);
        assertEquals("S2", get((EObject) get(t1, "target"), "name"));
        EObject action = (EObject) get(t1, "action");
        assertNotNull(action, "t1 should have action");
        assertEquals("CommunicationStmt", action.eClass().getName());
        EObject comm = unwrapComm(action);

        // Communication.value should be CallExp("v") from rv.value() pass-through
        EObject value = (EObject) get(comm, "value");
        assertNotNull(value, "Communication should have value from cast variable data flow");
        assertEquals("CallExp", value.eClass().getName());
        assertEquals("v", getCallExpName(value),
                "Value should be CallExp('v') representing event data pass-through");
    }

    @Test
    void actionEventMarkedTypedWhenArgPresent(TestInfo testInfo) throws IOException {
        writeStateMachineWithEventPayload();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> events = getChildren(stm, "events");

        // doAction event should also be marked as typed (it receives payload data)
        EObject doAction = events.stream()
                .filter(e -> "doAction".equals(get(e, "name")))
                .findFirst().orElseThrow();
        assertNotNull(get(doAction, "type"),
                "doAction event should have type set (receives cast variable data)");
    }

    @Test
    void patternVariableBackwardCompat(TestInfo testInfo) throws IOException {
        writeStateMachineWithEventPayloadPatternVar();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> events = getChildren(stm, "events");

        // goS2 (used with pattern variable rv) should have type set
        EObject goS2 = events.stream()
                .filter(e -> "goS2".equals(get(e, "name")))
                .findFirst().orElseThrow();
        assertNotNull(get(goS2, "type"),
                "goS2 event should have type set (pattern variable backward compat)");
    }

    // === E11: Value resolution from literals and constants ===

    @Test
    void resolvedConstantProducesRealExp(TestInfo testInfo) throws IOException {
        writeStateMachineWithLiteralAndConstant();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, discoverer.getResolvedValues());
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t1: S1 -> S2 with action doAction(MyConstants.SPEED=5.0)
        EObject t1 = transitions.get(1);
        assertEquals("S2", get((EObject) get(t1, "target"), "name"));
        EObject action = (EObject) get(t1, "action");
        assertNotNull(action, "t1 should have action");
        assertEquals("CommunicationStmt", action.eClass().getName());
        EObject comm = unwrapComm(action);

        // Communication.value should be FloatExp(5.0) from resolved constant
        EObject value = (EObject) get(comm, "value");
        assertNotNull(value, "Communication should have value from resolved constant");
        assertEquals("FloatExp", value.eClass().getName(),
                "Value should be FloatExp for double constant");
        assertEquals(5.0f, ((Number) get(value, "value")).floatValue(), 0.001f,
                "FloatExp value should be 5.0");
    }

    @Test
    void resolvedLiteralProducesIntegerExp(TestInfo testInfo) throws IOException {
        writeStateMachineWithLiteralAndConstant();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, discoverer.getResolvedValues());
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t2: S2 -> S3 with action doOther(42)
        EObject t2 = transitions.get(2);
        assertEquals("S3", get((EObject) get(t2, "target"), "name"));
        EObject action = (EObject) get(t2, "action");
        assertNotNull(action, "t2 should have action");
        assertEquals("CommunicationStmt", action.eClass().getName());
        EObject comm = unwrapComm(action);

        // Communication.value should be IntegerExp(42) from resolved literal
        EObject value = (EObject) get(comm, "value");
        assertNotNull(value, "Communication should have value from resolved literal");
        assertEquals("IntegerExp", value.eClass().getName(),
                "Value should be IntegerExp for integer literal");
        assertEquals(42, get(value, "value"),
                "IntegerExp value should be 42");
    }

    // === E11: Cross-branch channel consistency ===

    @Test
    void crossBranchTypedChannelGetsResolvedValue(TestInfo testInfo) throws IOException {
        writeStateMachineWithCrossBranchTyping();
        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, discoverer.getResolvedValues());
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // Find the guard-only branch (S1 → S2 with adviseVelocity(MyConstants.SAFE_SPEED))
        // This branch has no instanceof, so no pattern variable — but adviseVelocity is
        // typed by branch 1 (which has GoS2 instanceof + rv.value() pass-through).
        // The cross-branch fallback should resolve MyConstants.SAFE_SPEED = 3.5.
        boolean foundCrossBranchValue = false;
        for (EObject t : transitions) {
            EObject action = (EObject) get(t, "action");
            if (action == null) continue;

            // Look for the adviseVelocity action with FloatExp(3.5)
            // It could be wrapped in SeqStatement or be a direct CommunicationStmt
            List<EObject> comms = new java.util.ArrayList<>();
            if (action.eClass().getName().equals("CommunicationStmt")) {
                comms.add(unwrapComm(action));
            } else if (action.eClass().getName().equals("SeqStatement")) {
                for (EObject s : getChildren(action, "statements")) {
                    comms.add(unwrapComm(s));
                }
            }

            for (EObject comm : comms) {
                EObject evt = (EObject) get(comm, "event");
                if (evt != null && "adviseVelocity".equals(get(evt, "name"))) {
                    EObject value = (EObject) get(comm, "value");
                    if (value != null && "FloatExp".equals(value.eClass().getName())) {
                        float v = ((Number) get(value, "value")).floatValue();
                        if (Math.abs(v - 3.5f) < 0.001f) {
                            foundCrossBranchValue = true;
                        }
                    }
                }
            }
        }
        assertTrue(foundCrossBranchValue,
                "Guard-only branch should have adviseVelocity with RealExp(3.5) " +
                "from cross-branch channel consistency fallback");
    }

    // === E11: Multi-field record types ===

    @Test
    void multiFieldRecordProducesFieldBindings(TestInfo testInfo) throws IOException {
        writeStateMachineWithMultiFieldRecord();

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, null, discoverer.getRecordMetadata());
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t1: S1 -> S2 with trigger Move and action SeqStatement(doX(rv.x()), doY(rv.y()))
        EObject t1 = transitions.get(1);
        assertEquals("S2", get((EObject) get(t1, "target"), "name"));
        EObject action = (EObject) get(t1, "action");
        assertNotNull(action, "t1 should have action");
        assertEquals("SeqStatement", action.eClass().getName(),
                "Multiple method calls should produce SeqStatement");

        List<EObject> stmts = getChildren(action, "statements");
        assertEquals(2, stmts.size(), "SeqStatement should have 2 statements");

        // First action: doX with CallExp("v_x") for multi-field
        EObject stmt0 = stmts.get(0);
        assertEquals("CommunicationStmt", stmt0.eClass().getName());
        EObject comm0 = unwrapComm(stmt0);
        assertEquals("doX", get((EObject) get(comm0, "event"), "name"));
        EObject val0 = (EObject) get(comm0, "value");
        assertNotNull(val0, "doX should have value from multi-field record");
        assertEquals("CallExp", val0.eClass().getName());
        assertEquals("v_x", getCallExpName(val0),
                "First field should produce CallExp('v_x')");

        // Second action: doY with CallExp("v_y") for multi-field
        EObject stmt1 = stmts.get(1);
        assertEquals("CommunicationStmt", stmt1.eClass().getName());
        EObject comm1 = unwrapComm(stmt1);
        assertEquals("doY", get((EObject) get(comm1, "event"), "name"));
        EObject val1 = (EObject) get(comm1, "value");
        assertNotNull(val1, "doY should have value from multi-field record");
        assertEquals("CallExp", val1.eClass().getName());
        assertEquals("v_y", getCallExpName(val1),
                "Second field should produce CallExp('v_y')");
    }

    // === E1: Full expression translation (field reads, comparisons, method calls) ===

    @Test
    void fieldReadGuardProducesNamedCallExp(TestInfo testInfo) throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    private boolean armed = false;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1 && event instanceof MyEvent.GoS2 && armed) {
                            currentMode = MyMode.S2;
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t1: S1 -> S2, guard should be CallExp("armed") from field read, not guard_tN
        EObject t1 = transitions.get(1);
        EObject condition = (EObject) get(t1, "condition");
        assertNotNull(condition, "t1 should have a guard condition from field read");
        assertEquals("CallExp", condition.eClass().getName(),
                "Field read guard should be a CallExp");
        assertEquals("armed", getCallExpName(condition),
                "Guard should use field name 'armed', not synthetic guard_tN");
    }

    @Test
    void comparisonGuardProducesBinaryExpression(TestInfo testInfo) throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    private int speed = 0;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1 && event instanceof MyEvent.GoS2 && speed > 10) {
                            currentMode = MyMode.S2;
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, discoverer.getResolvedValues());
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t1: S1 -> S2, guard should be BinaryExpression(gt, CallExp("speed"), IntegerExp(10))
        EObject t1 = transitions.get(1);
        EObject condition = (EObject) get(t1, "condition");
        assertNotNull(condition, "t1 should have a guard condition from comparison");
        assertEquals("GreaterThan", condition.eClass().getName(),
                "Comparison guard should be a GreaterThan");

        // Left: CallExp("speed")
        EObject left = (EObject) get(condition, "left");
        assertNotNull(left, "GreaterThan should have left operand");
        assertEquals("CallExp", left.eClass().getName());
        assertEquals("speed", getCallExpName(left),
                "Left operand should be field name 'speed'");

        // Right: IntegerExp(10)
        EObject right = (EObject) get(condition, "right");
        assertNotNull(right, "BinaryExpression should have right operand");
        assertEquals("IntegerExp", right.eClass().getName(),
                "Right operand should be IntegerExp for literal 10");
        assertEquals(10, get(right, "value"),
                "IntegerExp value should be 10");
    }

    @Test
    void methodCallGuardProducesCallExpWithArgs(TestInfo testInfo) throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1 && event instanceof MyEvent.GoS2 && isReady(42)) {
                            currentMode = MyMode.S2;
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                    private boolean isReady(int threshold) { return true; }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, discoverer.getResolvedValues());
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t1: S1 -> S2, guard should be CallExp("isReady", [IntegerExp(42)])
        EObject t1 = transitions.get(1);
        EObject condition = (EObject) get(t1, "condition");
        assertNotNull(condition, "t1 should have a guard condition from method call");
        assertEquals("CallExp", condition.eClass().getName(),
                "Method call guard should be a CallExp");
        assertEquals("isReady", getCallExpName(condition),
                "CallExp should use method name 'isReady'");

        // Args: [IntegerExp(42)]
        List<EObject> args = getChildren(condition, "args");
        assertEquals(1, args.size(), "isReady(42) should have 1 argument");
        assertEquals("IntegerExp", args.get(0).eClass().getName(),
                "Argument should be IntegerExp");
        assertEquals(42, get(args.get(0), "value"),
                "Argument value should be 42");
    }

    // === E12: Configurable naming conventions ===

    @Test
    void customNamingConventionProducesStateMachine(TestInfo testInfo) throws IOException {
        writeSource("custom", "OperatingPhase.java", """
                package custom;
                public enum OperatingPhase { IDLE, RUNNING, STOPPED }
                """);
        writeSource("custom", "CustomEvent.java", """
                package custom;
                public sealed interface CustomEvent {
                    record Start() implements CustomEvent {}
                    record Stop() implements CustomEvent {}
                }
                """);
        writeSource("custom", "CustomController.java", """
                package custom;
                public final class CustomController {
                    private OperatingPhase state = OperatingPhase.IDLE;

                    public void process(CustomEvent event) {
                        if (state == OperatingPhase.IDLE && event instanceof CustomEvent.Start) {
                            state = OperatingPhase.RUNNING;
                        } else if (state == OperatingPhase.RUNNING && event instanceof CustomEvent.Stop) {
                            state = OperatingPhase.STOPPED;
                        } else if (state == OperatingPhase.STOPPED) {
                            state = OperatingPhase.IDLE;
                        }
                    }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(
                javaResource,
                List.of("Phase"),   // enum suffix
                "process",          // step method name
                "state"             // mode field name
        );
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);

        assertEquals("CustomController", get(stm, "name"));

        List<EObject> nodes = getChildren(stm, "nodes");
        List<EObject> states = nodes.stream()
                .filter(n -> n.eClass().getName().equals("State"))
                .toList();
        assertEquals(3, states.size(), "Expected 3 states from OperatingPhase enum");

        List<String> stateNames = states.stream()
                .map(s -> (String) get(s, "name"))
                .toList();
        assertTrue(stateNames.contains("IDLE"));
        assertTrue(stateNames.contains("RUNNING"));
        assertTrue(stateNames.contains("STOPPED"));

        // Initial transition should target IDLE (from field initialiser)
        List<EObject> transitions = getChildren(stm, "transitions");
        assertTrue(transitions.size() >= 3,
                "Expected at least 3 transitions, got " + transitions.size());
        EObject tInit = transitions.get(0);
        assertEquals("IDLE", get((EObject) get(tInit, "target"), "name"));
    }

    @Test
    void mismatchedNamingProducesEmptyModel(TestInfo testInfo) throws IOException {
        writeMinimalStateMachine(); // Uses MyMode/step/currentMode

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(
                javaResource,
                List.of("Phase"),   // won't match "MyMode"
                "process",          // won't match "step"
                "state"             // won't match "currentMode"
        );
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        List<EObject> machines = getChildren(rcPackage, "machines");
        assertEquals(0, machines.size(),
                "Mismatched naming should produce no state machines");
    }

    // === Predicate definition inlining (vel/hvel/vvel) ===

    @Test
    void inlinesComparisonPredicateInitializer(TestInfo testInfo) throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        writeSource("sm", "MySensor.java", """
                package sm;
                public class MySensor {
                    public double vel() { return 0.0; }
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    private MySensor sensor = new MySensor();
                    public void step(MyEvent event) {
                        boolean velBelowThreshold = sensor.vel() <= 0.1;
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2 && velBelowThreshold) {
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, discoverer.getResolvedValues());
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t1: S1 -> S2 with GoS2 trigger. Guard should be inlined comparison,
        // NOT CallExp("velBelowThreshold")
        EObject t1 = transitions.get(1);
        EObject condition = (EObject) get(t1, "condition");
        assertNotNull(condition, "t1 should have a guard condition");
        assertEquals("LessOrEqual", condition.eClass().getName(),
                "Guard should be LessOrEqual from inlined comparison, not CallExp");

        // Left: CallExp("vel") from sensor.vel()
        EObject left = (EObject) get(condition, "left");
        assertNotNull(left);
        assertEquals("CallExp", left.eClass().getName());
        assertEquals("vel", getCallExpName(left),
                "Left operand should be CallExp('vel') from method invocation");

        // Right: FloatExp(0.1) from literal
        EObject right = (EObject) get(condition, "right");
        assertNotNull(right);
        assertEquals("FloatExp", right.eClass().getName(),
                "Right operand should be FloatExp from literal 0.1");
    }

    @Test
    void keepsSimpleMethodCallPredicateName(TestInfo testInfo) throws IOException {
        // Same as the existing extractsTransitionsWithNamedPredicatesAndNesting test:
        // boolean ready = isReady() should NOT be inlined (not a comparison)
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        boolean ready = isReady();
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2 && ready) {
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                    private boolean isReady() { return true; }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource);
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);
        EObject stm = getChild(rcPackage, "machines", 0);
        List<EObject> transitions = getChildren(stm, "transitions");

        // t1: guard should remain CallExp("ready"), NOT inlined
        EObject t1 = transitions.get(1);
        EObject condition = (EObject) get(t1, "condition");
        assertNotNull(condition, "t1 should have a guard condition");
        assertEquals("CallExp", condition.eClass().getName(),
                "Non-comparison predicate should remain CallExp (backward compat)");
        assertEquals("ready", getCallExpName(condition),
                "Guard should use variable name 'ready'");
    }

    @Test
    void createsFunctionsForInlinedMethodCalls(TestInfo testInfo) throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        writeSource("sm", "MySensor.java", """
                package sm;
                public class MySensor {
                    public double vel() { return 0.0; }
                    public double hvel() { return 0.0; }
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    private MySensor sensor = new MySensor();
                    public void step(MyEvent event) {
                        boolean velBelowThreshold = sensor.vel() <= 0.1;
                        boolean hvelAbove = sensor.hvel() >= 0.2;
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2 && velBelowThreshold) {
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            if (hvelAbove) {
                                currentMode = MyMode.S1;
                            }
                        }
                    }
                }
                """);

        Resource javaResource = discoverer.discover(tempDir);
        Resource rcResource = transformer.transform(javaResource, discoverer.getResolvedValues());
        saveModel(rcResource, testInfo);

        EObject rcPackage = rcResource.getContents().get(0);

        // Zero-arg sensor methods referenced from inlined boolean predicates
        // are lifted into the Sensors interface as `var name : Type` (current
        // convention — see CLAUDE.md "Generated RoboChart structure"). The
        // older "create RoboChart Function" convention was retired in favour
        // of typed Sensors-interface vars so that the RoboChart CSP generator
        // can wire them as input channels rather than as function calls.
        EObject sensorsIface = null;
        for (EObject iface : getChildren(rcPackage, "interfaces")) {
            if ("Sensors".equals(get(iface, "name"))) {
                sensorsIface = iface;
                break;
            }
        }
        assertNotNull(sensorsIface, "Sensors interface should exist");
        List<EObject> varLists = getChildren(sensorsIface, "variableList");
        assertFalse(varLists.isEmpty(),
                "Sensors interface should have at least one VariableList");
        List<EObject> sensorVars = getChildren(varLists.get(0), "vars");
        List<String> sensorVarNames = sensorVars.stream()
                .map(v -> (String) get(v, "name"))
                .toList();
        assertTrue(sensorVarNames.contains("vel"),
                "Sensors should include var 'vel' from inlined predicate sensor.vel()");
        assertTrue(sensorVarNames.contains("hvel"),
                "Sensors should include var 'hvel' from inlined predicate sensor.hvel()");

        // Each sensor var should have type TypeRef -> PrimitiveType("real")
        for (EObject sv : sensorVars) {
            EObject typeObj = (EObject) get(sv, "type");
            assertNotNull(typeObj, "Sensor var " + get(sv, "name") + " should have a type");
            assertEquals("TypeRef", typeObj.eClass().getName(),
                    "Sensor var type should be a TypeRef");
            EObject refType = (EObject) get(typeObj, "ref");
            assertNotNull(refType, "TypeRef should have ref");
            assertEquals("PrimitiveType", refType.eClass().getName());
            assertEquals("real", get(refType, "name"),
                    "Sensor var type should be PrimitiveType('real')");
        }
    }

    // ========================================================================
    // Test fixtures
    // ========================================================================

    // Flat if-else pattern (Phase 1)
    private void writeMinimalStateMachine() throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2, S3 }
                """);

        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                    record GoS3() implements MyEvent {}
                }
                """);

        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;

                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1 && event instanceof MyEvent.GoS2) {
                            currentMode = MyMode.S2;
                        } else if (currentMode == MyMode.S2 && event instanceof MyEvent.GoS3) {
                            currentMode = MyMode.S3;
                        } else if (currentMode == MyMode.S3) {
                            currentMode = MyMode.S1;
                        }
                    }
                }
                """);
    }

    // Mode-nested if-else pattern (Phase 3 — pure mode-nested)
    private void writeModeNestedStateMachine() throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2, S3 }
                """);

        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                    record GoS3() implements MyEvent {}
                }
                """);

        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;

                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2) {
                                currentMode = MyMode.S2;
                            } else if (event instanceof MyEvent.GoS3) {
                                currentMode = MyMode.S3;
                            }
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                }
                """);
    }

    // E4: single method call + mode change
    private void writeStateMachineWithSingleAction() throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1 && event instanceof MyEvent.GoS2) {
                            doSomething();
                            currentMode = MyMode.S2;
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                    private void doSomething() {}
                }
                """);
    }

    // E4: multiple method calls + mode change
    private void writeStateMachineWithMultipleActions() throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1 && event instanceof MyEvent.GoS2) {
                            doFirst();
                            doSecond();
                            currentMode = MyMode.S2;
                        } else if (currentMode == MyMode.S2) {
                            currentMode = MyMode.S1;
                        }
                    }
                    private void doFirst() {}
                    private void doSecond() {}
                }
                """);
    }

    // E11: traditional instanceof + explicit cast with data flow
    private void writeStateMachineWithEventPayload() throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2(double value) implements MyEvent {}
                    record GoS1() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2) {
                                MyEvent.GoS2 rv = (MyEvent.GoS2) event;
                                doAction(rv.value());
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            if (event instanceof MyEvent.GoS1) {
                                currentMode = MyMode.S1;
                            }
                        }
                    }
                    private void doAction(double v) {}
                }
                """);
    }

    // E11 backward compat: pattern-matching instanceof with data flow
    private void writeStateMachineWithEventPayloadPatternVar() throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2(double value) implements MyEvent {}
                    record GoS1() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2 rv) {
                                doAction(rv.value());
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            if (event instanceof MyEvent.GoS1) {
                                currentMode = MyMode.S1;
                            }
                        }
                    }
                    private void doAction(double v) {}
                }
                """);
    }

    // E11: literal value and named constant resolution
    private void writeStateMachineWithLiteralAndConstant() throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2, S3 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2(double value) implements MyEvent {}
                    record GoS3(int count) implements MyEvent {}
                    record GoS1() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyConstants.java", """
                package sm;
                public final class MyConstants {
                    public static final double SPEED = 5.0;
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2) {
                                MyEvent.GoS2 rv = (MyEvent.GoS2) event;
                                doAction(MyConstants.SPEED);
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            if (event instanceof MyEvent.GoS3) {
                                MyEvent.GoS3 rc = (MyEvent.GoS3) event;
                                doOther(42);
                                currentMode = MyMode.S3;
                            }
                        } else if (currentMode == MyMode.S3) {
                            if (event instanceof MyEvent.GoS1) {
                                currentMode = MyMode.S1;
                            }
                        }
                    }
                    private void doAction(double v) {}
                    private void doOther(int c) {}
                }
                """);
    }

    // E11: multi-field record — Move(double x, double y) with per-field actions
    private void writeStateMachineWithMultiFieldRecord() throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record Move(double x, double y) implements MyEvent {}
                    record GoS1() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.Move) {
                                MyEvent.Move rv = (MyEvent.Move) event;
                                doX(rv.x());
                                doY(rv.y());
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            if (event instanceof MyEvent.GoS1) {
                                currentMode = MyMode.S1;
                            }
                        }
                    }
                    private void doX(double v) {}
                    private void doY(double v) {}
                }
                """);
    }

    // E11: cross-branch channel consistency — guard-only branch uses typed channel
    private void writeStateMachineWithCrossBranchTyping() throws IOException {
        writeSource("sm", "MyMode.java", """
                package sm;
                public enum MyMode { S1, S2, S3 }
                """);
        writeSource("sm", "MyEvent.java", """
                package sm;
                public sealed interface MyEvent {
                    record GoS2(double value) implements MyEvent {}
                    record GoS3() implements MyEvent {}
                }
                """);
        writeSource("sm", "MyConstants.java", """
                package sm;
                public final class MyConstants {
                    public static final double SAFE_SPEED = 3.5;
                }
                """);
        writeSource("sm", "MyController.java", """
                package sm;
                public final class MyController {
                    private MyMode currentMode = MyMode.S1;
                    public void step(MyEvent event) {
                        if (currentMode == MyMode.S1) {
                            if (event instanceof MyEvent.GoS2) {
                                MyEvent.GoS2 rv = (MyEvent.GoS2) event;
                                adviseVelocity(rv.value());
                                currentMode = MyMode.S2;
                            }
                        } else if (currentMode == MyMode.S2) {
                            adviseVelocity(MyConstants.SAFE_SPEED);
                            currentMode = MyMode.S3;
                        } else if (currentMode == MyMode.S3) {
                            if (event instanceof MyEvent.GoS3) {
                                currentMode = MyMode.S1;
                            }
                        }
                    }
                    private void adviseVelocity(double v) {}
                }
                """);
    }

    // ========================================================================
    // Model persistence
    // ========================================================================

    private static final Path OUTPUT_DIR = Path.of("build", "test-output");

    private void saveModel(Resource resource, TestInfo testInfo) throws IOException {
        Files.createDirectories(OUTPUT_DIR);
        String fileName = testInfo.getTestMethod().map(m -> m.getName()).orElse("unknown") + "_robochart.xmi";
        Path outputPath = OUTPUT_DIR.resolve(fileName);
        resource.setURI(URI.createFileURI(outputPath.toAbsolutePath().toString()));
        resource.save(null);
        System.out.println("Saved RoboChart model: " + outputPath);
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private void writeSource(String packageDir, String fileName, String source) throws IOException {
        Path dir = tempDir.resolve(packageDir);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(fileName), source);
    }

    private static Object get(EObject obj, String featureName) {
        EStructuralFeature f = obj.eClass().getEStructuralFeature(featureName);
        return f != null ? obj.eGet(f) : null;
    }

    /** Extract the name from a CallExp by unwrapping StringExp in the function containment ref. */
    private static String getCallExpName(EObject callExp) {
        EObject func = (EObject) get(callExp, "function");
        return func != null ? (String) get(func, "value") : null;
    }

    /** Unwrap CommunicationStmt to get the inner Communication, or return as-is. */
    private static EObject unwrapComm(EObject obj) {
        if (obj != null && "CommunicationStmt".equals(obj.eClass().getName())) {
            return (EObject) get(obj, "communication");
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    private static List<EObject> getChildren(EObject obj, String featureName) {
        Object val = get(obj, featureName);
        return val instanceof List<?> list ? (List<EObject>) list : List.of();
    }

    private static EObject getChild(EObject obj, String featureName, int index) {
        return getChildren(obj, featureName).get(index);
    }
}
