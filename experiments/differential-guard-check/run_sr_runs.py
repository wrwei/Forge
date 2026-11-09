#!/usr/bin/env python3
"""Differential guard check on the converged code of the SRanger loop runs (secondary)."""
import collections, csv, glob, math, os, random, re, subprocess, sys
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(HERE, "..", "requirements-constants-check"))
from common import *
from model_eval import Real, Env, parse_theory, admitted, Undecided, as_double_env
from check_stated_values import java_sources, declared_constants, norm
from run_runs import converged, CEnv

up = math.nextafter
MODES = ["Moving", "Turning", "Final"]; EVENTS = ["endTask", "obstacle", "tick", "none"]


def scen(settable, R):
    ds = [0.0, 0.5, up(0.5, 1), up(0.5, 0), 0.1 + 0.4, 1000.0, -1.0, 0.49, 0.51]
    if settable:   # clock can be set: put now - reset on, just below and just above the 2.0 duration, and rounding cases
        ts = [(10.0, 8.0), (10.0, up(8.0, 0)), (10.0, up(8.0, 9)), (2.3, 0.3), (0.3, -1.7), (100.0, 0.0), (0.0, 0.0)]
        rnd = lambda: (lambda n: (n, R.choice([n - 2.0, n - R.uniform(0, 4), round(n - R.uniform(0, 4), 1)])))(round(R.uniform(0, 50), 1))
    else:          # wall-clock time: only resets far in the past or future decide the comparison
        ts = [(0.0, -1e300), (0.0, 1e300)]
        rnd = lambda: (0.0, R.choice([-1e300, 1e300]))
    b = [(d, n, c) for d in ds for (n, c) in ts]
    r = [(R.choice([R.uniform(-0.5, 2.0), round(R.uniform(0, 1), 2), 0.5]), *rnd()) for _ in range(2000)]
    return b, r


def run(run_dir, tag, out_dir, seed=20261109):
    it = converged(run_dir); th = parse_theory(glob.glob(f"{it}/formal-artefacts/isabelle/*_Beh.thy")[0])
    consts = {norm(k): v for k, v in declared_constants(java_sources(f"{it}/java")).items()}
    modes_thy = {m.lower(): m for o in th["ops"] for m in re.findall(r"st\s*=\s*(\w+)", o["pre"])}
    B = f"/tmp/dgc/{tag}"; os.makedirs(B, exist_ok=True)
    files = [os.path.join(d, f) for d, _, fs in os.walk(f"{it}/java") for f in fs if f.endswith(".java") and "/app/" not in d]
    r = subprocess.run([f"{JAVA}/javac", "-d", B] + files + [f"{HERE}/harness/Refl.java", f"{HERE}/harness/GenericSrHarness.java"], capture_output=True, text=True)
    if r.returncode: raise RuntimeError(r.stderr[-2000:])
    probe = run_harness(B, "harness.GenericSrHarness", ["p Moving none 1 0 0"])["p"]
    settable = probe["fields"]["SETTABLE"] == "true"
    b, rr = scen(settable, random.Random(seed)); rows = []
    for kind, gen in (("boundary", b), ("random", rr)):
        modes = sorted(v for k, v in modes_thy.items() if k != "initial")
        lines = [f"{kind[0]}{i}_{m}_{e} {m} {e} {d!r} {n!r} {c!r}" for i, (d, n, c) in enumerate(gen) for m in modes for e in EVENTS]
        res = run_harness(B, "harness.GenericSrHarness", lines)
        for l in lines:
            sid, mode, ev = l.split()[:3]; rec = res[sid]
            funcs = {k: Real.of(dbl(v)) for k, v in rec["funcs"].items()}
            nowv = Real.of(dbl(rec["fields"]["NOW"])); funcs["since"] = (lambda n: (lambda x: n - x))(nowv)
            for n, vals in consts.items(): funcs.setdefault(n, Real.of(vals[0][1]))
            env = CEnv(th, funcs, {}); jm = modes_thy.get(rec["next"].lower(), rec["next"]); tm = modes_thy[mode.lower()]
            evt = None if ev == "none" else ev
            try:
                adm = admitted(th, env, tm, evt); verdict = "agree" if adm == [jm] else "disagree"
            except Undecided: adm, verdict = [], "undecided"
            cause = ""
            if verdict == "disagree":
                denv = as_double_env(env); denv.__class__ = CEnv
                denv.funcs["since"] = (lambda n: (lambda x: n - x))(float(nowv.q))
                cause = "double-arithmetic" if admitted(th, denv, tm, evt) == [jm] else "unexplained"
            rows.append(dict(run=tag, set=kind, id=sid, mode=mode, event=ev, java=rec["next"], model="|".join(adm), verdict=verdict,
                             cause=cause, clock_settable=settable, inputs=" ".join(l.split()[3:])))
    with open(f"{out_dir}/{tag}.csv", "w", newline="") as fh:
        w = csv.DictWriter(fh, fieldnames=list(rows[0])); w.writeheader(); w.writerows(rows)
    return rows


if __name__ == "__main__":
    X, out = sys.argv[1], sys.argv[2]
    for r in sys.argv[3:]:
        rows = run(f"{X}/sranger/run-{r}", f"sranger-run{r}", out)
        print(f"sranger run-{r} settable={rows[0]['clock_settable']}", collections.Counter((x["verdict"], x["cause"]) for x in rows),
              collections.Counter((x["mode"], x["java"], x["model"], x["inputs"]) for x in rows if x["verdict"] != "agree").most_common(4))
