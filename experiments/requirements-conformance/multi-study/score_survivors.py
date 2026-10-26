"""Score the SRanger and Chemical Detector mutants that survive every verifier
(experiments/mutation-testing, kill_table.csv rows with disposition == survived)
against the compiled requirements relations (requirements_model_{sranger,cd_ga,cd_mv}.json).

Kill = at some flag-consistent cell where the unmutated model's successor set is a
member of the admitted set, the mutant model's is not; or a numeric guard bound in the
mutant theory diverges from the requirement's threshold.  For Chemical Detector
mutants both controllers are scored and either can kill.  Idle_<State> operations are
not transitions (see run_reqconf_multi.py); with --idle-check the script verifies, per
theory, that each Idle_<State> precondition is enabled exactly when no other
operation of that state is.

Usage, from the repository root:
    python3 experiments/requirements-conformance/multi-study/score_survivors.py [--check] [--idle-check FILE]
"""
import json, os, sys, csv
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import run_reqconf_multi as rm
from run_reqconf_multi import ARMS, enumerate_arm, theory_path, CAMPAIGN_OUT

def rows_for(arm, thy_path, here):
    rows, unknown, foreign = enumerate_arm(arm, thy_path, here)
    if unknown:
        raise RuntimeError(f"unevaluated atoms for {arm} @ {thy_path}: {sorted(unknown)}")
    return rows, foreign

def rows_map(arm, thy_path, here):
    rows, foreign = rows_for(arm, thy_path, here)
    key = lambda r: (r["mode"], r["event"]) + tuple(sorted((k, v) for k, v in r.items()
                    if k not in ("arm","mode","event","flags_consistent","R_set","M_set","required","verdict")))
    return {key(r): r for r in rows}, foreign

def idle_check_rows(mutant_root):
    """For every parsed theory: is each Idle_<State> precondition the complement of its siblings' enabled sets?"""
    import re, itertools
    IN = "\\<in>"
    out = []
    for path, idle in rm.IDLE_OPS.items():
        if not idle: continue
        arm = {"SRangerController_Beh.thy": "sranger", "GasAnalysisController_Beh.thy": "cd_ga",
               "MovementController_Beh.thy": "cd_mv"}[os.path.basename(path)]
        relf, cfg, thyname, evals = rm.ARMS[arm]
        ops, listens = rm._orig_parse_thy(path)
        sib = [o for o in ops if not o["name"].startswith("Idle_") and o["state"] not in (None, "initial")]
        base = rm.base_sem(arm)
        theory = os.path.relpath(path, mutant_root) if os.path.abspath(path).startswith(os.path.abspath(mutant_root)) else path
        for o in idle:
            ok, bad = True, []
            gv = rm.gvar_items(cfg)
            for vals in itertools.product(*[v for _, v in gv]):
                gdict = dict(zip([k for k, _ in gv], vals))
                for ev in cfg["events"]:
                    ev_thy = cfg["ev_thy"][ev]
                    lset = listens.get(o["state"], set())
                    offered = {ev_thy} if (ev_thy is not None and ev_thy in lset) else set()
                    def base_ev(a_, gg, _off=offered):
                        m = re.fullmatch(r"(\w+)\s*" + re.escape(IN) + r"\s*(?:offered|triggers)", a_.strip())
                        if m: return m.group(1) in _off
                        return base(a_, gg)
                    unknown, foreign = set(), set()
                    en = True
                    for a_ in rm.rejoin_atoms(o["atoms"]):
                        v = rm.eval_expr(a_, gdict, base_ev, unknown, foreign)
                        if v is None: en = None; break
                        if not v: en = False; break
                    if en is None:
                        ok = False; bad.append(f"{ev}:unevaluable {sorted(unknown)}"); continue
                    if en and (any(e not in offered for e in o["presence"]) or any(e in offered for e in o["absence"])):
                        en = False
                    sib_en = bool(rm.model_admits2(arm, sib, listens, o["state"], gdict, ev_thy, set(), set()))
                    if en != (not sib_en):
                        ok = False; bad.append(f"{ev}/{gdict}")
            out.append(dict(theory=theory, arm=arm, idle_op=o["name"], state=o["state"], target=o["target"],
                            self_loop=(o["target"] == o["state"]), complement_of_siblings=ok, detail=";".join(bad[:5])))
    return out

def main():
    import argparse
    ap = argparse.ArgumentParser()
    here0 = os.path.dirname(os.path.abspath(__file__))
    ap.add_argument("--baseline-root", default=CAMPAIGN_OUT, help="dir holding baseline-<study>/isabelle/ (default: the campaign outputs)")
    ap.add_argument("--mutant-root", default=CAMPAIGN_OUT, help="dir holding <mutant_id>/isabelle/")
    ap.add_argument("--pool", default=os.path.join(here0, "..", "..", "mutation-testing", "kill_table.csv"),
                    help="the campaign's kill table; SRanger and Chemical Detector rows with disposition == survived")
    ap.add_argument("--ids", default=None, help="instead of --pool: a plain id list, one per line")
    ap.add_argument("--out", default=os.path.join(here0, "multi_survivor_scoring.csv"))
    ap.add_argument("--idle-check", default=None, help="CSV with the Idle_<State> complement check per theory")
    ap.add_argument("--check", action="store_true",
                    help="compare the freshly computed scoring against the shipped multi_survivor_scoring.csv")
    a = ap.parse_args()
    here = os.path.dirname(os.path.abspath(__file__))
    base = {}
    for arm, (_, _, thyname, _) in ARMS.items():
        st = "sranger" if arm == "sranger" else "chemical_detector"
        bm, bf = rows_map(arm, theory_path(a.baseline_root, st, thyname), here)
        assert not bf, f"baseline {arm} has foreign atoms: {bf}"
        base[arm] = bm
    out = []
    if a.ids:
        ids = [l.strip() for l in open(a.ids) if l.strip()]
    else:
        ids = [r["mutant_id"] for r in csv.DictReader(open(a.pool))
               if r["study"] in ("sranger", "chemical_detector") and r["disposition"] == "survived"]
    for mid in ids:
        arms = ["sranger"] if mid.startswith("sranger") else ["cd_ga", "cd_mv"]
        killed, mechs, witness, scoreable = False, [], "", True
        for arm in arms:
            _, _, thyname, _ = ARMS[arm]
            tp = os.path.join(a.mutant_root, mid, "isabelle", thyname)
            if not os.path.exists(tp):
                scoreable = False; mechs.append(f"{arm}: no regenerated theory"); continue
            try:
                mut, foreign = rows_map(arm, tp, here)
            except Exception as e:
                scoreable = False; mechs.append(f"{arm}: parse/eval failure: {e}"); continue
            if foreign:
                killed = True
                witness = witness or f"{arm}: guard predicate diverges: {sorted(foreign)[0]}"
                mechs.append(f"{arm}: guard-bound divergence ({'; '.join(sorted(foreign))})")
                continue
            viol = [k for k, r in base[arm].items()
                    if r["flags_consistent"] and r["verdict"] == "member"
                    and (k not in mut or mut[k]["verdict"] != "member")]
            ident = all(k in mut and mut[k]["M_set"] == base[arm][k]["M_set"] for k in base[arm])
            if viol:
                killed = True
                k0 = viol[0]; mw = mut.get(k0)
                witness = witness or f"{arm}:{k0[0]}+{k0[1]} base={base[arm][k0]['M_set']} mut={(mw or {}).get('M_set','absent')}"
                mechs.append(f"{arm}: {len(viol)} membership-violation cells")
            elif ident:
                mechs.append(f"{arm}: theory verdict-identical to baseline")
            else:
                mechs.append(f"{arm}: successors differ but stay within the admitted sets")
        out.append(dict(mutant_id=mid, scoreable=scoreable, killed=killed and scoreable,
                        mechanism="; ".join(mechs), witness=witness))
    with open(a.out, "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=["mutant_id","scoreable","killed","mechanism","witness"])
        w.writeheader()
        for r in out: w.writerow(r)
    ks = sum(r["killed"] for r in out); ns = sum(not r["scoreable"] for r in out)
    print(f"scored {len(out)} survivors: killed={ks} unscoreable={ns}")
    print("wrote", a.out)
    if a.idle_check:
        rows = idle_check_rows(a.mutant_root)
        with open(a.idle_check, "w", newline="") as f:
            w = csv.DictWriter(f, fieldnames=["theory", "arm", "idle_op", "state", "target", "self_loop", "complement_of_siblings", "detail"])
            w.writeheader(); [w.writerow(r) for r in rows]
        nok = sum(r["complement_of_siblings"] for r in rows)
        print(f"Idle check: {len(rows)} Idle operations over {len({r['theory'] for r in rows})} theories; complement property holds for {nok}/{len(rows)}")
    if a.check:
        ship = os.path.join(here, "multi_survivor_scoring.csv")
        same = open(a.out).read() == open(ship).read()
        print("CHECK:", "rows byte-identical to the shipped scoring" if same else "ROWS DIFFER from the shipped scoring")
        sys.exit(0 if same else 1)

if __name__ == "__main__":
    main()
