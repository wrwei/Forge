#!/usr/bin/env python3
"""Build the 35 behaviour-preserving mutants from the reference Java (reference-runs/<study>/java)."""
import csv, os, re, shutil, sys
HERE = os.path.dirname(os.path.abspath(__file__))
REF = os.path.join(HERE, "..", "..", "reference-runs")
OUT = os.path.join(HERE, "mutants")


def sub1(t, old, new):
    n = t.count(old); assert n == 1, (n, old[:80]); return t.replace(old, new)


def suball(t, word, new):
    t2, n = re.subn(r"\b%s\b" % re.escape(word), new, t); assert n >= 1, word; return t2


def swap_lines(t, anchor, a, b):
    """Swap the first line containing `a` and the first containing `b` after `anchor`."""
    i = t.index(anchor); L = t[i:].split("\n")
    ia = next(k for k, l in enumerate(L) if a in l); ib = next(k for k, l in enumerate(L) if b in l)
    L[ia], L[ib] = L[ib], L[ia]; return t[:i] + "\n".join(L)


def add_helper(t, cls_anchor, method):
    i = t.rindex("\n}"); return t[:i] + "\n\n" + method + "\n}" + t[i + 2:]


def add_counter(t, field_anchor, step_sig):
    t = sub1(t, field_anchor, field_anchor + "\n    private int stepCount = 0;")
    return sub1(t, step_sig, step_sig + "\n        this.stepCount = this.stepCount + 1;")


LC = "controller/LreController.java"; SC = "controller/SRangerController.java"
GC = "gasanalysis/GasAnalysisController.java"; MC = "movement/MovementController.java"
M = []  # (id, study, class, {file: fn} or ("all", fn))
def m(i, st, cl, f, fn): M.append((i, st, cl, f, fn))

# ---- LRE
m("lre_bp_rename_01", "lre", "rename", LC, lambda t: suball(t, "velBelowOrAtOne", "slowEnough"))
m("lre_bp_rename_02", "lre", "rename", LC, lambda t: suball(t, "rv", "request"))
m("lre_bp_negcmp_01", "lre", "negated comparison", LC, lambda t: sub1(t, "boolean velBelowOrAtOne = vel <= 1.0;", "boolean velBelowOrAtOne = !(vel > 1.0);"))
m("lre_bp_swapop_01", "lre", "swapped operands", LC, lambda t: sub1(t, "boolean cdaAboveOrAtMinSafe = cda >= LreConstants.minSafeDist;", "boolean cdaAboveOrAtMinSafe = LreConstants.minSafeDist <= cda;"))
m("lre_bp_swapop_02", "lre", "swapped operands", LC, lambda t: sub1(t, "&& this.tcpa >= 0.0;", "&& 0.0 <= this.tcpa;"))
m("lre_bp_inline_01", "lre", "inlined local", LC, lambda t: sub1(t, "&& odistCstcAboveOne) {", "&& sensor.odist(cstc) > 1.0) {"))
m("lre_bp_helper_01", "lre", "helper method", LC, lambda t: add_helper(sub1(t, "boolean cdaAboveOrAtMinSafe = cda >= LreConstants.minSafeDist;", "boolean cdaAboveOrAtMinSafe = cdaSafe();"), None,
  "    private boolean cdaSafe() {\n        return cda >= LreConstants.minSafeDist;\n    }"))
m("lre_bp_reorder_01", "lre", "reordered statements", LC, lambda t: swap_lines(t, "public void step(", "calcCStc.compute();", "calcCDyn.compute();"))
m("lre_bp_reorder_02", "lre", "reordered statements", LC, lambda t: swap_lines(t, "else if (event instanceof InputEvent.ReqHCM) {", "currentMode = LreMode.HCM;", "actuator.receive(new OutputEvent.AdvVel(0.0));"))
m("lre_bp_reorder_03", "lre", "reordered statements", LC, lambda t: swap_lines(t, "public void step(", "this.hvel = calcVel.hvel();", "this.vvel = calcVel.vvel();"))
m("lre_bp_counter_01", "lre", "step counter", LC, lambda t: add_counter(t, "private LreMode currentMode = LreMode.OCM;", "public void step(InputEvent event) {"))
m("lre_bp_constname_01", "lre", "renamed constant", "*", lambda t: re.sub(r"\bminSafeDist\b", "minimumSafeDistance", t))
# ---- SRanger
m("sranger_bp_rename_01", "sranger", "rename", SC, lambda t: suball(t, "obstacleDetected", "obstacleClose"))
m("sranger_bp_rename_02", "sranger", "rename", SC, lambda t: suball(t, "turnDurationElapsed", "turnDone"))
m("sranger_bp_negcmp_01", "sranger", "negated comparison", SC, lambda t: sub1(t, "sensor.distance() <= SRangerConstants.obstacleThreshold;", "!(sensor.distance() > SRangerConstants.obstacleThreshold);"))
m("sranger_bp_swapop_01", "sranger", "swapped operands", SC, lambda t: sub1(t, "sensor.distance() <= SRangerConstants.obstacleThreshold;", "SRangerConstants.obstacleThreshold >= sensor.distance();"))
m("sranger_bp_inline_01", "sranger", "inlined local", SC, lambda t: sub1(t, "event instanceof InputEvent.Obstacle && obstacleDetected", "event instanceof InputEvent.Obstacle && sensor.distance() <= SRangerConstants.obstacleThreshold"))
m("sranger_bp_helper_01", "sranger", "helper method", SC, lambda t: add_helper(sub1(t, "boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;", "boolean obstacleDetected = obstacleNear();"), None,
  "    private boolean obstacleNear() {\n        return sensor.distance() <= SRangerConstants.obstacleThreshold;\n    }"))
m("sranger_bp_reorder_01", "sranger", "reordered statements", SC, lambda t: swap_lines(t, "event instanceof InputEvent.Obstacle && obstacleDetected", "currentMode = SRangerMode.Turning;", "turnTimer.markReset();"))
m("sranger_bp_reorder_02", "sranger", "reordered statements", SC, lambda t: swap_lines(t, "if (turnDurationElapsed) {", "currentMode = SRangerMode.Moving;", "actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));"))
m("sranger_bp_counter_01", "sranger", "step counter", SC, lambda t: add_counter(t, "    private boolean turnDurationElapsed;", "public void step(InputEvent event) {"))
m("sranger_bp_constname_01", "sranger", "renamed constant", "*", lambda t: re.sub(r"\bobstacleThreshold\b", "obstacleDistanceThreshold", t))
# ---- gas analysis
m("chemical_detector_bp_rename_01", "chemical_detector", "rename", GC, lambda t: suball(t, "insAboveThr", "intensityHigh"))
m("chemical_detector_bp_swapop_01", "chemical_detector", "swapped operands", GC, lambda t: sub1(t, "boolean insAboveThr = insVal >= 10.0;", "boolean insAboveThr = 10.0 <= insVal;"))
m("chemical_detector_bp_negcmp_01", "chemical_detector", "negated comparison", GC, lambda t: sub1(t, "boolean insAboveThr = insVal >= 10.0;", "boolean insAboveThr = !(insVal < 10.0);"))
m("chemical_detector_bp_swapop_02", "chemical_detector", "swapped operands", GC, lambda t: sub1(t, "boolean stsIsNoGas = sts == Status.noGas;", "boolean stsIsNoGas = Status.noGas == sts;"))
m("chemical_detector_bp_reorder_01", "chemical_detector", "reordered statements", GC, lambda t: swap_lines(t, "if (stsIsNoGas) {", "this.bus.publish(new MovementEvent.Resume());", "currentMode = GasAnalysisMode.NoGas;"))
m("chemical_detector_bp_counter_01", "chemical_detector", "step counter", GC, lambda t: add_counter(t, "    private final EventBus bus;", "public void step(GasAnalysisEvent e) {"))
# ---- movement
m("chemical_detector_bp_rename_02", "chemical_detector", "rename", MC, lambda t: suball(t, "distanceProgressed", "movedEnough"))
m("chemical_detector_bp_swapop_03", "chemical_detector", "swapped operands", MC, lambda t: sub1(t, "boolean distanceProgressed = d1 - d0 > ChemDetectorConstants.stuckDist;", "boolean distanceProgressed = ChemDetectorConstants.stuckDist < d1 - d0;"))
m("chemical_detector_bp_negcmp_02", "chemical_detector", "negated comparison", MC, lambda t: sub1(t, "boolean distanceStagnant = d1 - d0 <= ChemDetectorConstants.stuckDist;", "boolean distanceStagnant = !(d1 - d0 > ChemDetectorConstants.stuckDist);"))
m("chemical_detector_bp_reorder_02", "chemical_detector", "reordered statements", MC, lambda t: swap_lines(t, "} else if (e instanceof MovementEvent.Obstacle) {", "this.clockResetTime = clock.nowMs();", "currentMode = MovementMode.Avoiding;"))
m("chemical_detector_bp_reorder_03", "chemical_detector", "reordered statements", MC, lambda t: swap_lines(t, "vehicle.randomWalk();", "vehicle.flag();", "vehicle.move(0.0, Angle.Front);"))
m("chemical_detector_bp_counter_02", "chemical_detector", "step counter", MC, lambda t: add_counter(t, "    private final Vehicle vehicle;", "public void step(MovementEvent e) {"))
m("chemical_detector_bp_constname_01", "chemical_detector", "renamed constant", "*", lambda t: re.sub(r"\bstuckDist\b", "stuckDistance", t))
assert len(M) == 35, len(M)

if __name__ == "__main__":
    if os.path.exists(OUT): shutil.rmtree(OUT)
    rows = []
    for mid, st, cl, f, fn in M:
        src = os.path.join(REF, st, "java"); dst = os.path.join(OUT, mid, "java"); shutil.copytree(src, dst)
        files = [f] if f != "*" else [os.path.relpath(os.path.join(d, x), dst) for d, _, xs in os.walk(dst) for x in xs if x.endswith(".java")]
        changed = []
        for rel in files:
            p = os.path.join(dst, rel); t = open(p).read(); t2 = fn(t)
            if t2 != t: open(p, "w").write(t2); changed.append(rel)
        assert changed, mid
        rows.append(dict(mutant_id=mid, study=st, bp_class=cl, files=";".join(changed)))
    with open(os.path.join(HERE, "mutants_manifest.csv"), "w", newline="") as fh:
        w = csv.DictWriter(fh, fieldnames=list(rows[0])); w.writeheader(); w.writerows(rows)
    print(len(rows), "mutants")
