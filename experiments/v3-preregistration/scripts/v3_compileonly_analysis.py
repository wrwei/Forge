"""Pre-registration v3, Amendment 18 (analysis fixed in advance) and Amendment 19 (re-scoring).

For each Compile-only run: the stop-point iteration and, at the stop point, the recorded
verdict of each withheld phase. Per study: how many of the five runs stop at an iteration
whose withheld phases all pass, and for the others which withheld phase fails. Two
comparisons, counts only: against the Reference runs and against the cold passes.
An iteration re-scored under Amendment 19 (rescore-a19/iter-<i>/phase_status.json) is
counted by its re-scored verdict; the recorded verdict is kept beside it.
"""
import glob, json, os, re, sys
sys.path.insert(0, os.path.dirname(__file__))
from v3_analysis import E, PHASES, iteration, collect  # readers shared with the main analysis

OUT = os.path.join(E, "v3-analysis")
STUDIES = ["sranger", "lre", "chemical_detector"]
WITHHELD = ["fdr4", "dafny_verify", "isabelle_verify", "vacuity"]
VISIBLE = [p for p in PHASES if p not in WITHHELD]


def collect_compileonly(study):
    runs = []
    for r in sorted(glob.glob(f"{E}/v3-compile-only/{study}/run-*"), key=lambda x: int(x.rsplit("-", 1)[1])):
        its = sorted((i for i in glob.glob(f"{r}/iter-*") if re.fullmatch(r"iter-\d+", os.path.basename(i))),
                     key=lambda x: int(os.path.basename(x).split("-")[1]))
        rec = {"run": int(r.rsplit("-", 1)[1]), "iterations": [iteration(i) for i in its]}
        for k, it in enumerate(rec["iterations"]):
            rs = os.path.join(r, "rescore-a19", f"iter-{it['iter']}", "phase_status.json")
            if os.path.exists(rs):
                ps = json.load(open(rs))["phase_status"]
                it["recorded_verdict"] = {"sha": it["sha"], "phase_status": dict(it["phase_status"]),
                                          "failed": list(it["failed"]), "first_message": it["first_message"]}
                failed = [p for p in PHASES if ps.get(p) != "passed"]
                it.update(phase_status=ps, failed=failed, first_failed=failed[0] if failed else None,
                          first_message="", cleared=next((i for i, p in enumerate(PHASES) if ps.get(p) != "passed"), len(PHASES)),
                          sha="1ad9301e (Amendment 19 rescore)")
        stop = rec["iterations"][-1]
        assert all(stop["phase_status"].get(p) == "passed" for p in VISIBLE), (r, "stop point must pass every visible phase")
        rec["stop_iteration"] = stop["iter"]
        rec["withheld_at_stop"] = {p: stop["phase_status"].get(p) for p in WITHHELD}
        rec["withheld_failed"] = [p for p in WITHHELD if stop["phase_status"].get(p) != "passed"]
        rec["verification_passing_stop"] = not rec["withheld_failed"]
        rec["visible_failed_before_stop"] = [[p for p in VISIBLE if it["phase_status"].get(p) != "passed"] for it in rec["iterations"][:-1]]
        runs.append(rec)
    return runs


def main():
    co = {s: collect_compileonly(s) for s in STUDIES}
    ref = {s: collect("v3-reference", s) for s in STUDIES}
    cold = {s: collect("v3-cold", s) for s in STUDIES}
    table = {}
    for s in STUDIES:
        R = co[s]
        table[s] = {
            "runs": len(R),
            "stop_at_iteration_1": sum(r["stop_iteration"] == 1 for r in R),
            "stop_iterations": [r["stop_iteration"] for r in R],
            "verification_passing_stop": sum(r["verification_passing_stop"] for r in R),
            "withheld_failures": {f"run-{r['run']}": r["withheld_failed"] for r in R if r["withheld_failed"]},
            "rescored_runs": [f"run-{r['run']}" for r in R if any("recorded_verdict" in it for it in r["iterations"])],
            "rescored_recorded_failures": {f"run-{r['run']}": it["recorded_verdict"]["failed"] for r in R for it in r["iterations"] if "recorded_verdict" in it},
            "visible_failures_before_stop": {f"run-{r['run']}": r["visible_failed_before_stop"] for r in R if r["visible_failed_before_stop"]},
            # comparisons, counts only
            "reference_converged": sum(r["converged"] for r in ref[s]),
            "reference_runs": len(ref[s]),
            "reference_converged_at_1": sum(r["converged"] and r["iterations_used"] == 1 for r in ref[s]),
            "reference_iterations": [r["iterations_used"] for r in ref[s]],
            "cold_all_passed": sum(r["converged"] for r in cold[s]),
            "cold_passes": len(cold[s]),
            # a Compile-only run's iteration 1 is a cold pass by construction
            "compileonly_iter1_all_twelve_passed": sum(not r["iterations"][0]["failed"] for r in R),
        }
    tot = {k: sum(table[s][k] for s in STUDIES) for k in
           ("runs", "stop_at_iteration_1", "verification_passing_stop", "reference_converged", "reference_runs",
            "reference_converged_at_1", "cold_all_passed", "cold_passes", "compileonly_iter1_all_twelve_passed")}
    out = {"compileonly": co, "table": table, "total": tot,
           "pins": {"compileonly": sorted({it["sha"][:8] for s in STUDIES for r in co[s] for it in r["iterations"]}),
                    "reference": sorted({it["sha"][:8] for s in STUDIES for r in ref[s] for it in r["iterations"]})}}
    json.dump(out, open(os.path.join(OUT, "compileonly_analysis.json"), "w"), indent=1)
    L = ["# Compile-only condition (Amendments 18 and 19)", "",
         "| Study | Runs | Stop at iteration 1 | Verification-passing stop | Withheld failure at stop | Re-scored (A19) | Reference converged (at it. 1) | Cold passes all twelve |",
         "|---|---|---|---|---|---|---|---|"]
    for s in STUDIES + ["total"]:
        t = table[s] if s != "total" else tot
        wf = "; ".join(f"{k}: {', '.join(v)}" for k, v in table[s]["withheld_failures"].items()) if s != "total" else ""
        rs = ", ".join(table[s]["rescored_runs"]) if s != "total" else ""
        L.append(f"| {s} | {t['runs']} | {t['stop_at_iteration_1']} | {t['verification_passing_stop']} | {wf} | {rs} | {t['reference_converged']}/{t['reference_runs']} ({t['reference_converged_at_1']}) | {t['cold_all_passed']}/{t['cold_passes']} |")
    L += ["", f"Compile-only iteration 1 passing all twelve phases (equivalent to a cold pass): {tot['compileonly_iter1_all_twelve_passed']} of {tot['runs']}.",
          f"Pins: Compile-only {out['pins']['compileonly']}; Reference {out['pins']['reference']}."]
    open(os.path.join(OUT, "compileonly_analysis.md"), "w").write("\n".join(L) + "\n")
    print("\n".join(L))


if __name__ == "__main__":
    main()
