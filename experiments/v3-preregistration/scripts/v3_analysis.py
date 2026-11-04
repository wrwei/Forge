#!/usr/bin/env python3
"""Pre-registered analysis of the study-free collections (prereg-v3-study-free.md,
"Analysis, fixed in advance", items 1-5).

    python3 scripts/v3_analysis.py            # writes experiments/v3-analysis/

Reads only the committed records under experiments/v3-cold and experiments/v3-reference.
Cold Chemical Detector pass 3 is scored by its Amendment 2 rescore (rescore-2db4c031),
with its verdict at 5280e26e reported beside it.
"""
import glob, json, math, os, re, statistics, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
E = os.path.join(ROOT, "experiments")
OUT = os.path.join(E, "v3-analysis")
PHASES = ["compile", "coverage", "preflight", "t2m", "m2m", "m2t", "dafny_gen",
          "isabelle_gen", "fdr4", "dafny_verify", "isabelle_verify", "vacuity"]
LETTER = {"compile": "J", "coverage": "C", "preflight": "P", "t2m": "T", "m2m": "R",
          "m2t": "M", "dafny_gen": "G", "isabelle_gen": "H", "fdr4": "F",
          "dafny_verify": "D", "isabelle_verify": "I", "vacuity": "V"}
STUDIES = ["sranger", "lre", "chemical_detector"]

# Classification of every failing phase, as in analysis item 1:
# (a) a verdict on the code; (b) Java outside the written instructions that no check
# detects; (c) a pipeline defect on Java within the instructions; (e) no verdict:
# the check did not finish within the machine's memory or the pipeline's time limits. Read from each report's
# raw message and the session's own summary of the repair.
CLASS = {
 ("v3-cold", "lre", 10, 1, "preflight"): ("a", "the profile checker rejects a double constant without @RoboChartType(\"real\") (rule 4)"),
 ("v3-cold", "chemical_detector", 3, 1, "m2t@5280e26e"): ("c", "the extractor omitted a function called inside an operation-call argument, so the RoboChart model referenced an undeclared function; fixed by Amendment 2"),
 ("v3-reference", "chemical_detector", 2, 1, "preflight"): ("c", "the profile checker reports GasSensor.i as a double without @RoboChartType(\"real\"), but the record component carries the annotation (record GasSensor(Chem c, @RoboChartType(\"real\") double i)); the checker misses annotations on record components, a false positive"),
 ("v3-reference", "chemical_detector", 4, 1, "fdr4"): ("b", "operation classes with compute(Loc side) and compute(double speed, Angle heading), against rule 5 (one public void compute() method); the profile checker does not test it; the extractor merged the methods and the CSP is ill-typed"),
 ("v3-reference", "chemical_detector", 4, 1, "isabelle_verify"): ("c", "the Isabelle generator typed the inlined parameter `side` (a Loc) as real (its usage guess); re-scored at the Amendment 14 pin d714365c, which takes the type the model declares, this phase passes (rescore-a14/iter-1); the FDR4 failure of the same iteration remains (b)"),
 ("v3-reference", "chemical_detector", 4, 2, "isabelle_verify"): ("c", "the same typing defect: the argument-taking compute methods remain (rule 5) and their bodies are inlined into Init, but the failure is the parameter `side` typed real; re-scored at d714365c, iteration 2 passes all twelve phases (rescore-a14/iter-2)"),
 ("v3-cold", "steam_boiler", 1, 1, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 40960 MB limit (peak: 41402 MB)"),
 ("v3-cold", "steam_boiler", 1, 1, "dafny_verify"): ("b", "two enums share value names (INITIALIZATION, DEGRADED), against the rule that each name in the model is unique within its scope (java_codegen_rules.txt, naming section); Dafny resolves the value to the wrong type"),
 ("v3-cold", "steam_boiler", 1, 1, "isabelle_verify"): ("b", "the same enum value-name clash; Isabelle mistypes ModeValue.DEGRADED"),
 ("v3-cold", "steam_boiler", 2, 1, "preflight"): ("a", "the profile checker rejects 1 double field(s) without @RoboChartType(\"real\"), which rule 4 requires"),
 ("v3-cold", "steam_boiler", 2, 1, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 40960 MB limit (peak: 40982 MB)"),
 ("v3-cold", "steam_boiler", 2, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-cold", "steam_boiler", 3, 1, "fdr4"): ("e", "no verdict: FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 81199 MB)."),
 ("v3-cold", "steam_boiler", 3, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-cold", "steam_boiler", 4, 1, "preflight"): ("c", "the profile checker rejects 4 double parameter(s) without @RoboChartType(\"real\"); rule 4 requires the annotation only on double fields (scoring note of 3 November 2026)"),
 ("v3-cold", "steam_boiler", 4, 1, "fdr4"): ("e", "no verdict: FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 76213 MB)."),
 ("v3-cold", "steam_boiler", 4, 1, "isabelle_verify"): ("c", "the Isabelle generator emitted empty right-hand sides for the rescue-mode level estimates (levelLow', levelHigh') in five operations although the RoboChart model carries the expressions; inner syntax error"),
 ("v3-cold", "steam_boiler", 4, 1, "vacuity"): ("c", "the vacuity audit crashed in its own guard parser (maximum recursion depth exceeded)"),
 ("v3-cold", "steam_boiler", 5, 1, "preflight"): ("c", "the profile checker rejects 5 double parameter(s) without @RoboChartType(\"real\"); rule 4 requires the annotation only on double fields (scoring note of 3 November 2026)"),
 ("v3-cold", "steam_boiler", 5, 1, "fdr4"): ("e", "no verdict: FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 74544 MB)."),
 ("v3-cold", "steam_boiler", 5, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-cold", "steam_boiler", 6, 1, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 81920 MB limit (peak: 81925 MB)"),
 ("v3-cold", "steam_boiler", 6, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-cold", "steam_boiler", 6, 1, "vacuity"): ("c", "the vacuity audit crashed in its own guard parser (maximum recursion depth exceeded)"),
 ("v3-cold", "steam_boiler", 7, 1, "preflight"): ("a", "the profile checker rejects 6 double field(s) without @RoboChartType(\"real\"), which rule 4 requires (and 5 parameter(s))"),
 ("v3-cold", "steam_boiler", 7, 1, "fdr4"): ("e", "no verdict: FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 72031 MB)."),
 ("v3-cold", "steam_boiler", 7, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-cold", "steam_boiler", 8, 1, "preflight"): ("c", "the profile checker rejects 2 double parameter(s) without @RoboChartType(\"real\"); rule 4 requires the annotation only on double fields (scoring note of 3 November 2026)"),
 ("v3-cold", "steam_boiler", 8, 1, "fdr4"): ("e", "no verdict: FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 79113 MB)."),
 ("v3-cold", "steam_boiler", 8, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-cold", "steam_boiler", 8, 1, "vacuity"): ("c", "the vacuity audit crashed in its own guard parser (maximum recursion depth exceeded)"),
 ("v3-cold", "steam_boiler", 9, 1, "preflight"): ("c", "the profile checker rejects 2 double parameter(s) without @RoboChartType(\"real\"); rule 4 requires the annotation only on double fields (scoring note of 3 November 2026)"),
 ("v3-cold", "steam_boiler", 9, 1, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 81920 MB limit (peak: 82128 MB)"),
 ("v3-cold", "steam_boiler", 9, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-cold", "steam_boiler", 10, 1, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 81920 MB limit (peak: 81992 MB)"),
 ("v3-cold", "steam_boiler", 10, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 1, 1, "preflight"): ("a", "the profile checker rejects 8 double field(s) without @RoboChartType(\"real\"), which rule 4 requires (and 2 parameter(s))"),
 ("v3-reference", "steam_boiler", 1, 1, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 40960 MB limit (peak: 41396 MB)"),
 ("v3-reference", "steam_boiler", 1, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 1, 1, "vacuity"): ("c", "the vacuity audit crashed in its own guard parser (maximum recursion depth exceeded)"),
 ("v3-reference", "steam_boiler", 1, 2, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 40960 MB limit (peak: 41226 MB)"),
 ("v3-reference", "steam_boiler", 1, 2, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 1, 3, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 40960 MB limit (peak: 41374 MB)"),
 ("v3-reference", "steam_boiler", 1, 3, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 2, 1, "preflight"): ("c", "the profile checker rejects 1 double parameter(s) without @RoboChartType(\"real\"); rule 4 requires the annotation only on double fields (scoring note of 3 November 2026)"),
 ("v3-reference", "steam_boiler", 2, 1, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 81920 MB limit (peak: 82152 MB)"),
 ("v3-reference", "steam_boiler", 2, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 2, 1, "vacuity"): ("c", "the vacuity audit crashed in its own guard parser (maximum recursion depth exceeded)"),
 ("v3-reference", "steam_boiler", 2, 2, "fdr4"): ("e", "no verdict: FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 79880 MB)."),
 ("v3-reference", "steam_boiler", 2, 2, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 2, 3, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 81920 MB limit (peak: 81973 MB)"),
 ("v3-reference", "steam_boiler", 2, 3, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 2, 4, "fdr4"): ("e", "no verdict: FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 77110 MB)."),
 ("v3-reference", "steam_boiler", 2, 4, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 2, 5, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 3, 1, "preflight"): ("c", "the profile checker rejects 1 double parameter(s) without @RoboChartType(\"real\"); rule 4 requires the annotation only on double fields (scoring note of 3 November 2026)"),
 ("v3-reference", "steam_boiler", 3, 1, "fdr4"): ("e", "no verdict: FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 81558 MB)."),
 ("v3-reference", "steam_boiler", 3, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 3, 2, "fdr4"): ("e", "no verdict: FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 71255 MB)."),
 ("v3-reference", "steam_boiler", 3, 2, "vacuity"): ("c", "the vacuity audit crashed in its own guard parser (maximum recursion depth exceeded)"),
 ("v3-reference", "steam_boiler", 4, 1, "preflight"): ("c", "the profile checker rejects 2 double parameter(s) without @RoboChartType(\"real\"); rule 4 requires the annotation only on double fields (scoring note of 3 November 2026)"),
 ("v3-reference", "steam_boiler", 4, 1, "fdr4"): ("e", "no verdict: FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 78305 MB)."),
 ("v3-reference", "steam_boiler", 4, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 4, 1, "vacuity"): ("c", "the vacuity audit crashed in its own guard parser (maximum recursion depth exceeded)"),
 ("v3-reference", "steam_boiler", 4, 2, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 81920 MB limit (peak: 82174 MB)"),
 ("v3-reference", "steam_boiler", 4, 2, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 4, 3, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 81920 MB limit (peak: 82062 MB)"),
 ("v3-reference", "steam_boiler", 4, 3, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 4, 4, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 81920 MB limit (peak: 82568 MB)"),
 ("v3-reference", "steam_boiler", 5, 1, "preflight"): ("a", "the profile checker rejects 1 double field(s) without @RoboChartType(\"real\"), which rule 4 requires"),
 ("v3-reference", "steam_boiler", 5, 1, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 81920 MB limit (peak: 82020 MB)"),
 ("v3-reference", "steam_boiler", 5, 1, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 5, 1, "vacuity"): ("c", "the vacuity audit crashed in its own guard parser (maximum recursion depth exceeded)"),
 ("v3-reference", "steam_boiler", 5, 2, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 81920 MB limit (peak: 81973 MB)"),
 ("v3-reference", "steam_boiler", 5, 2, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
 ("v3-reference", "steam_boiler", 5, 3, "fdr4"): ("e", "no verdict: FDR4 killed: memory usage exceeded 81920 MB limit (peak: 82021 MB)"),
 ("v3-reference", "steam_boiler", 5, 3, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof did not finish within the pipeline's per-goal timeout"),
}

# Entries for the Steam Boiler Reference run superseded by Amendment 15
# (experiments/v3-superseded-a15); kept for the record, not part of the collections.
SUPERSEDED_A15_CLASS = {
 ("v3-reference", "steam_boiler", 1, 1, "preflight"): ("a", "the profile checker rejects double parameters of a private helper without @RoboChartType(\"real\"); the rules say to annotate parameters and require the annotation explicitly on every double field"),
 ("v3-reference", "steam_boiler", 1, 1, "m2t"): ("c", "a record component named `on`, a keyword of RoboChart's textual notation that the rules' reserved-word list (rule 3) does not include; the generated .rct does not parse (the report labels this a Java compile error)"),
 ("v3-reference", "steam_boiler", 1, 1, "fdr4"): ("-", "not run: CSP generation (M2T) failed"),
 ("v3-reference", "steam_boiler", 1, 1, "isabelle_verify"): ("c", "the Isabelle generator inlined a compound predicate and typed a bare boolean sensor call as real; rule 4(a) allows predicates built from earlier predicates"),
 ("v3-reference", "steam_boiler", 1, 2, "fdr4"): ("c", "the extractor turns an actuator call into an event and drops its argument, but an int parameter still types the event; the CSP is ill-typed (Int=>InOut vs InOut); no rule restricts actuator method parameters, and Layer 3 describes actuators as setters"),
 ("v3-reference", "steam_boiler", 1, 2, "isabelle_verify"): ("c", "a predicate used only inside another predicate typed as real in the theory"),
 ("v3-reference", "steam_boiler", 1, 3, "fdr4"): ("e", "no verdict: macOS ended refines after 961 s at a 98,418 MB footprint on the 48 GiB machine; the report labels this a parse error"),
 ("v3-reference", "steam_boiler", 1, 3, "isabelle_verify"): ("e", "no verdict: the deadlock-freedom proof (35 transitions in the extracted model) did not finish within the pipeline's per-goal limit; the re-check on a quiet machine (feedback/isabelle_rerun_1) ended the same way after 9 min 57 s, so machine load did not cause it; iteration 4's 21-transition model is proved in 3 min 18 s"),
}


def fisher_one_sided(a, n1, b, n2):
    """P(X >= a) for the first group under the hypergeometric null (group 1 = loop)."""
    K, N = a + b, n1 + n2
    def pmf(x):
        return math.comb(K, x) * math.comb(N - K, n1 - x) / math.comb(N, n1)
    return sum(pmf(x) for x in range(a, min(K, n1) + 1))


def first_issue(fb_dir, phase):
    p = os.path.join(fb_dir, f"post_{phase}.md")
    if not os.path.exists(p):
        return ""
    t = open(p, errors="ignore").read()
    blocks = re.split(r"^(?=### Issue \d+: )", t, flags=re.M)[1:]
    # the first issue that is not a warning or an advisory (a warning never fails a phase)
    for b in blocks:
        if not re.search(r"^Severity: (warning|advisory)\b|\[ADVISORY\]", b, re.M | re.I):
            head = re.match(r"### Issue \d+: ([^\n]+)", b).group(1).strip()
            raw = re.search(r"\*\*Raw\*\*\s*```\s*\n(.*?)```", b, re.S)
            err = [l.strip() for l in (raw.group(1).splitlines() if raw else []) if l.strip().startswith(("ERROR", "Message:", "***", "Error"))]
            return head + (f" [{err[0][:200]}]" if err else "")
    s = re.search(r"^## Summary\s*\n+([^\n]+)", t, re.M)
    return (s.group(1) if s else "").strip()


def iteration(d):
    s = json.load(open(os.path.join(d, "summary.json")))
    ps = s["phase_status"]
    t = os.path.join(d, "feedback", "phase_timings.json")
    wall = None
    if os.path.exists(t):
        wall = sum(v.get("wall_clock_s") or 0 for v in json.load(open(t))["phases"].values())
    failed = [p for p in PHASES if ps.get(p) != "passed"]
    first = failed[0] if failed else None
    return {"iter": s["iteration"], "sha": s["infrastructure_sha"][:8], "phase_status": ps,
            "failed": failed, "first_failed": first,
            "first_message": first_issue(os.path.join(d, "feedback"), first) if first else "",
            "cleared": next((i for i, p in enumerate(PHASES) if ps.get(p) != "passed"), len(PHASES)),
            "java_files": s.get("files", {}).get("java"), "java_loc": s.get("java_loc"),
            "pipeline_wall_s": wall}


def collect(coll, study):
    runs = []
    for r in sorted(glob.glob(f"{E}/{coll}/{study}/run-*"), key=lambda x: int(x.rsplit("-", 1)[1])):
        its = sorted(glob.glob(f"{r}/iter-*"), key=lambda x: int(re.sub(r"\D", "", os.path.basename(x)) or 0))
        its = [i for i in its if re.fullmatch(r"iter-\d+", os.path.basename(i))]
        rec = {"run": int(r.rsplit("-", 1)[1]), "iterations": [iteration(i) for i in its]}
        rs = os.path.join(r, "rescore-2db4c031", "phase_status.json")
        if os.path.exists(rs):
            ps = json.load(open(rs))["phase_status"]
            rec["recorded_verdict_at_" + rec["iterations"][0]["sha"]] = dict(rec["iterations"][0])
            failed = [p for p in PHASES if ps.get(p) != "passed"]
            rec["iterations"][0] = dict(rec["iterations"][0], phase_status=ps, failed=failed,
                                        first_failed=failed[0] if failed else None, first_message="",
                                        cleared=next((i for i, p in enumerate(PHASES) if ps.get(p) != "passed"), len(PHASES)),
                                        sha="2db4c031 (Amendment 2 rescore)")
        last = rec["iterations"][-1]
        rec["converged"] = not last["failed"]
        rec["iterations_used"] = len(rec["iterations"])
        runs.append(rec)
    return runs


def main():
    os.makedirs(OUT, exist_ok=True)
    cold = {s: collect("v3-cold", s) for s in STUDIES}
    ref = {s: collect("v3-reference", s) for s in STUDIES + ["steam_boiler"]}
    cold_sb = collect("v3-cold", "steam_boiler")   # Amendments 11, 12, 15
    for s in STUDIES:
        assert len(cold[s]) == 10, (s, len(cold[s]))
        assert len(ref[s]) == 5, (s, len(ref[s]))
    # Steam Boiler: re-collected under Amendment 15 (10 cold passes, 5 Reference runs);
    # reported in Section 5 with however many sessions are committed.

    md = ["# Study-free collections: pre-registered analysis", "",
          "Generated by `scripts/v3_analysis.py` from the committed records.", ""]

    # 1. Cold (Table 5)
    md += ["## 1. Cold passes (Table 5)", "", "| Study | Converged | Not converged (first failing phase: message) |", "|---|---|---|"]
    t5 = {}
    for s in STUDIES:
        conv = sum(r["converged"] for r in cold[s])
        nc = [(r["run"], r["iterations"][0]["first_failed"], r["iterations"][0]["first_message"]) for r in cold[s] if not r["converged"]]
        t5[s] = {"converged": conv, "of": 10, "not_converged": nc}
        md.append(f"| {s} | {conv} of 10 | " + ("; ".join(f"pass {n}: {p}: {m}" for n, p, m in nc) or "none") + " |")
    tot = sum(v["converged"] for v in t5.values())
    md += [f"| all | {tot} of 30 | |", ""]
    for s in STUDIES:
        for r in cold[s]:
            for k, v in r.items():
                if k.startswith("recorded_verdict_at_"):
                    md.append(f"Cold {s} pass {r['run']}: counted verdict from the Amendment 2 rescore (converged: {r['converged']}); "
                              f"recorded verdict at {k[-8:]}: first failing phase {v['first_failed']} ({v['first_message'] or 'no message'}).")
    md.append("")

    # 2. Reference (Table 7)
    md += ["## 2. Reference runs (Table 7)", "", "| Study | Run | Converged | Iterations | Failures (phase letters, iteration) | Pipeline wall-clock |", "|---|---|---|---|---|---|"]
    for s in STUDIES + ["steam_boiler"]:
        for r in ref[s]:
            fails = ", ".join("".join(LETTER[p] for p in it["failed"]) + f"({it['iter']})" for it in r["iterations"] if it["failed"]) or "---"
            wall = sum(it["pipeline_wall_s"] or 0 for it in r["iterations"])
            md.append(f"| {s} | {r['run']} | {'yes' if r['converged'] else 'no'} | {r['iterations_used']} | {fails} | {wall/60:.1f} min |")
    md += ["", "Letters: " + ", ".join(f"{v} = {k}" for k, v in LETTER.items()), ""]
    used15 = [r["iterations_used"] if r["converged"] else 7 for s in STUDIES for r in ref[s]]
    ref_conv = sum(r["converged"] for s in STUDIES for r in ref[s])
    for s in STUDIES:
        u = [r["iterations_used"] for r in ref[s]]
        md.append(f"{s}: converged {sum(r['converged'] for r in ref[s])} of 5; iterations median {statistics.median(u)} (range {min(u)}-{max(u)})")
    md += [f"All three studies: converged {ref_conv} of 15; iterations median {statistics.median(used15)}", ""]

    # 3. Best-of-N (Table 6)
    nmatch = math.ceil(statistics.median(used15))
    md += ["## 3. Best-of-N (Table 6)", "",
           f"Matching budget: median iterations of the 15 Reference runs = {statistics.median(used15)}, rounded up: N = {nmatch}.", "",
           "| N | Groups per study | Groups in all | Oracle-free converged | Verifier-informed converged | p vs Reference (one-sided Fisher) |", "|---|---|---|---|---|---|"]
    t6 = []
    for N in (1, 2, 3, 5, 10):
        g = 10 // N; of = vi = 0
        for s in STUDIES:
            for k in range(g):
                grp = cold[s][k * N:(k + 1) * N]
                of += any(r["converged"] for r in grp)
                best = max(grp, key=lambda r: (r["iterations"][0]["cleared"], -r["run"]))
                vi += best["converged"]
        p_of = fisher_one_sided(ref_conv, 15, of, 3 * g)
        p_vi = fisher_one_sided(ref_conv, 15, vi, 3 * g)
        t6.append({"N": N, "groups": 3 * g, "oracle_free": of, "verifier_informed": vi, "p_oracle_free": p_of, "p_verifier_informed": p_vi})
        md.append(f"| {N}{' (matching)' if N == nmatch else ''} | {g} | {3*g} | {of} of {3*g} | {vi} of {3*g} | {p_of:.3g}" + ("" if p_of == p_vi else f" / {p_vi:.3g}") + " |")
    md.append("")

    # 4. Same-condition check
    md += ["## 4. Same-condition check (no test)", "", "| Study | Cold converged | Reference converged at iteration 1 |", "|---|---|---|"]
    for s in STUDIES:
        it1 = sum(not r["iterations"][0]["failed"] for r in ref[s])
        md.append(f"| {s} | {t5[s]['converged']} of 10 | {it1} of 5 |")
    md.append("")

    # 5. Steam Boiler (RQ8). Amendment 12's analysis, on the Amendment 16 re-collection
    # (pin aaddf222, Amendment 17's FDR4 settings); the sessions collected at earlier pins
    # are in experiments/v3-superseded-a15 and v3-superseded-a16 and are not counted.
    sbr = ref["steam_boiler"]
    assert len(cold_sb) == 10 and len(sbr) == 5, (len(cold_sb), len(sbr))
    md += ["## 5. Steam Boiler (RQ8)", "", "### 5.1 Cold passes", "",
           "| Pass | Converged | First failing phase (class): message | Other failing phases (class) |", "|---|---|---|---|"]
    sb_first = {}
    for r in cold_sb:
        it = r["iterations"][0]
        if r["converged"]:
            md.append(f"| {r['run']} | yes | --- | --- |"); continue
        f0 = it["first_failed"]; c0 = CLASS[("v3-cold", "steam_boiler", r["run"], 1, f0)][0]
        sb_first[r["run"]] = c0
        rest = ", ".join(f"{p} ({CLASS[('v3-cold', 'steam_boiler', r['run'], 1, p)][0]})" for p in it["failed"][1:])
        md.append(f"| {r['run']} | no | {f0} ({c0}): {it['first_message']} | {rest or '---'} |")
    sb_conv = sum(r["converged"] for r in cold_sb)
    cnt = {c: list(sb_first.values()).count(c) for c in "abce"}
    md += ["", f"Converged {sb_conv} of 10. First failing phase by class: " + ", ".join(f"({c}) {n}" for c, n in cnt.items()) + ".", ""]
    md += ["### 5.2 Reference runs", "", "| Run | Converged | Iterations | Failures (phase letters, iteration) | Pipeline wall-clock |", "|---|---|---|---|---|"]
    for r in sbr:
        fails = ", ".join("".join(LETTER[p] for p in it["failed"]) + f"({it['iter']})" for it in r["iterations"] if it["failed"]) or "---"
        md.append(f"| {r['run']} | {'yes' if r['converged'] else 'no'} | {r['iterations_used']} | {fails} | {sum(it['pipeline_wall_s'] or 0 for it in r['iterations'])/60:.1f} min |")
    sb_used = [r["iterations_used"] if r["converged"] else 7 for r in sbr]
    sb_ref_conv = sum(r["converged"] for r in sbr)
    sb_match = math.ceil(statistics.median(sb_used))
    md += ["", f"Converged {sb_ref_conv} of 5; iterations median {statistics.median(sb_used)} (range {min(sb_used)}-{max(sb_used)}).", ""]
    md += ["### 5.3 Best-of-N", "", f"Matching budget: median iterations of the five Reference runs = {statistics.median(sb_used)}, rounded up: N = {sb_match}"
           + ("" if sb_match in (1, 2, 3, 5, 10) else " (not among the pre-registered N; reported as an extra row)") + ".", "",
           "| N | Groups | Oracle-free converged | Verifier-informed converged | p vs Reference (one-sided Fisher) |", "|---|---|---|---|---|"]
    sb_t6 = []
    for N in sorted(set((1, 2, 3, 5, 10, sb_match))):
        g = 10 // N
        grps = [cold_sb[k * N:(k + 1) * N] for k in range(g)]
        of = sum(any(r["converged"] for r in grp) for grp in grps)
        vi = sum(max(grp, key=lambda r: (r["iterations"][0]["cleared"], -r["run"]))["converged"] for grp in grps)
        p_of = fisher_one_sided(sb_ref_conv, 5, of, g); p_vi = fisher_one_sided(sb_ref_conv, 5, vi, g)
        sb_t6.append({"N": N, "groups": g, "oracle_free": of, "verifier_informed": vi, "p_oracle_free": p_of, "p_verifier_informed": p_vi})
        md.append(f"| {N}{' (matching)' if N == sb_match else ''} | {g} | {of} of {g} | {vi} of {g} | {p_of:.3g}" + ("" if p_of == p_vi else f" / {p_vi:.3g}") + " |")
    sb_it1 = sum(not r["iterations"][0]["failed"] for r in sbr)
    md += ["", "### 5.4 Same-condition check (no test)", "", f"Cold converged {sb_conv} of 10; Reference converged at iteration 1: {sb_it1} of 5.", "",
           "### 5.5 Cold converged rate beside the original studies (no test)", "", "| Study | Cold converged |", "|---|---|"]
    for s_ in STUDIES:
        md.append(f"| {s_} | {t5[s_]['converged']} of 10 |")
    md += [f"| steam_boiler | {sb_conv} of 10 |", ""]
    md += ["### 5.6 Per-phase verdicts per iteration", ""]
    for coll, runs in (("v3-cold", cold_sb), ("v3-reference", sbr)):
        for sb in runs:
            md.append(f"#### {coll} run {sb['run']}: converged {sb['converged']}, iterations {sb['iterations_used']}")
            md += ["", "| Iteration | " + " | ".join(PHASES) + " |", "|---|" + "---|" * len(PHASES)]
            for it in sb["iterations"]:
                md.append(f"| {it['iter']} | " + " | ".join(it["phase_status"].get(p, "") for p in PHASES) + " |")
            md.append("")

    # 6. Re-scores at the Amendment 14 pin (sessions whose extracted artefacts change there)
    md += ["## 6. Re-scores at the Amendment 14 pin d714365c", "",
           "| Session | Iteration | Failed as recorded | Failed at d714365c |", "|---|---|---|---|"]
    for rs in sorted(glob.glob(os.path.join(E, "v3-*", "*", "run-*", "rescore-a14", "iter-*", "phase_status.json"))):
        parts = rs.split(os.sep); it = int(parts[-2][5:]); run = parts[-4]; st = parts[-5]; coll = parts[-6]
        new = [p for p, v in json.load(open(rs))["phase_status"].items() if v != "passed"]
        old = [p for p, v in json.load(open(os.path.join(E, coll, st, run, f"iter-{it}", "summary.json")))["phase_status"].items() if v != "passed"]
        md.append(f"| {coll} {st} {run} | {it} | {', '.join(old) or 'none'} | {', '.join(new) or 'none (all twelve phases pass)'} |")
    md.append("")

    md += ["## Classification of every failing phase", "",
           "(a) verdict on the code; (b) Java outside the written instructions, undetected; (c) pipeline defect on Java within the instructions; (e) no verdict within the machine's memory or the pipeline's time limits; - not run.", "",
           "| Collection | Study | Run | Iteration | Phase | Class | Reason |", "|---|---|---|---|---|---|---|"]
    seen = set()
    for coll, runs in (("v3-cold", {**cold, "steam_boiler": cold_sb}), ("v3-reference", ref)):
        for st, rs in runs.items():
            for r in rs:
                for it in r["iterations"]:
                    for ph in it["failed"]:
                        k = (coll, st, r["run"], it["iter"], ph)
                        assert k in CLASS, f"unclassified failure {k}"
                        seen.add(k); c, why = CLASS[k]
                        md.append(f"| {coll} | {st} | {r['run']} | {it['iter']} | {ph} | {c} | {why} |")
    k = ("v3-cold", "chemical_detector", 3, 1, "m2t@5280e26e"); seen.add(k)
    md.append(f"| v3-cold | chemical_detector | 3 | 1 | m2t, at 5280e26e (reported beside the counted verdict) | {CLASS[k][0]} | {CLASS[k][1]} |")
    live = {k for k in CLASS if os.path.isdir(os.path.join(E, k[0], k[1], f"run-{k[2]}"))}
    assert seen == live, live - seen
    md.append("")
    open(os.path.join(OUT, "analysis.md"), "w").write("\n".join(md) + "\n")
    json.dump({"cold": cold, "cold_steam_boiler": cold_sb, "reference": ref, "table5": t5, "table6": t6, "matching_N": nmatch,
               "reference_converged": ref_conv, "steam_boiler": {"cold_converged": sb_conv, "cold_first_class": sb_first,
               "reference_converged": sb_ref_conv, "matching_N": sb_match, "best_of_n": sb_t6, "reference_it1_converged": sb_it1}}, open(os.path.join(OUT, "analysis.json"), "w"), indent=1)
    print("\n".join(md))


if __name__ == "__main__":
    main()
