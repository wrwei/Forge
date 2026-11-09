#!/usr/bin/env python3
"""Differential guard check for SRanger and the Chemical Detector's two controllers."""
import collections, csv, itertools, math, os, random, sys
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, HERE)
from common import *
from model_eval import Real, Env, parse_theory, admitted, Undecided, as_double_env

up = math.nextafter

SPECS = {
 "sranger": dict(harness="SRangerHarness", src="sranger", thy="baseline-sranger/isabelle/SRangerController_Beh.thy",
     modes=["Moving", "Turning", "Final"], events=["endTask", "obstacle", "tick", "none"], enums=()),
 "gas": dict(harness="GasHarness", src="chemical_detector", thy="baseline-chemical_detector/isabelle/GasAnalysisController_Beh.thy",
     modes=["Reading", "Analysis", "NoGas", "GasDetected", "Final"], events=["gas", "tick", "none"], enums=("noGas", "gasD")),
 "movement": dict(harness="MovementHarness", src="chemical_detector", thy="baseline-chemical_detector/isabelle/MovementController_Beh.thy",
     modes=["Waiting", "Going", "Avoiding", "TryingAgain", "AvoidingAgain", "GettingOut", "Found", "Final"],
     events=["turn", "stop", "resume", "obstacle", "tick", "none"], enums=()),
}


def scenarios(study, R):
    if study == "sranger":
        ds = [0.0, 0.5, up(0.5, 1), up(0.5, 0), 0.1 + 0.4, 0.2 + 0.3, 1000.0, -1.0, 5e-324, 1e300, 0.49, 0.51]
        b = [(d, e) for d in ds for e in ("0", "1")]
        r = [(R.choice([R.uniform(-0.5, 2.0), round(R.uniform(0, 1), 2), 0.5]), R.choice("01")) for _ in range(2000)]
        fmt = lambda sc: f"{sc[0]!r} {sc[1]}"
    elif study == "gas":
        vs = [10.0, up(10.0, 11), up(10.0, 9), 0.0, 9.999999, 11.0, -1.0, 1e300, 0.1 * 100, 3.3 * 3, 1e-300]
        b = [(s, v) for s in ("noGas", "gasD") for v in vs]
        r = [(R.choice(["noGas", "gasD"]), R.choice([R.uniform(0, 20), round(R.uniform(9, 11), 1), 10.0])) for _ in range(2000)]
        fmt = lambda sc: f"{sc[0]} {sc[1]!r}"
    else:
        ds = [(0.0, 0.0), (0.0, 1.0), (0.0, up(1.0, 2)), (0.0, up(1.0, 0)), (0.1, 1.1), (-0.7, 0.3), (0.2, 1.2),
              (1.0, 2.0), (0.0, 2.0), (0.3, 1.3), (2.5, 3.5)]
        ts = [(100, 100), (100, 96), (100, 95), (100, 94), (100, 0), (5, 0)]
        b = [(n, c, d0, d1) for (n, c) in ts for (d0, d1) in ds]
        def rd():
            d0 = R.choice([0.0, round(R.uniform(-3, 3), 1), R.uniform(-3, 3)])
            return d0, R.choice([d0 + 1.0, d0 + R.uniform(0, 2), round(d0 + R.uniform(0, 2), 1)])
        r = [(1000, 1000 - R.randint(0, 8), *rd()) for _ in range(2000)]
        fmt = lambda sc: f"{sc[0]} {sc[1]} {sc[2]!r} {sc[3]!r}"
    return b, r, fmt


def run(study, thy_root, src_root, out_dir, seed=20261109):
    S = SPECS[study]; th = parse_theory(os.path.join(thy_root, S["thy"]))
    B = f"/tmp/dgc/{S['harness']}"
    compile_harness(os.path.join(src_root, S["src"], "java"), os.path.join(HERE, "harness", S["harness"] + ".java"), B)
    compile_harness(os.path.join(src_root, S["src"], "java"), os.path.join(HERE, "harness", "Refl.java"), B)
    b, r, fmt = scenarios(study, random.Random(seed)); rows = []
    for kind, gen in (("boundary", b), ("random", r)):
        lines = [f"{kind[0]}{i}_{m}_{e} {m} {e} {fmt(sc)}" for i, sc in enumerate(gen) for m in S["modes"] for e in S["events"]]
        res = run_harness(B, f"harness.{S['harness']}", lines)
        for l in lines:
            sid, mode, ev = l.split()[:3]; rec = res[sid]; f = rec["fields"]
            funcs, state = {}, {}
            for k, v in rec["funcs"].items(): funcs[k] = Real.of(dbl(v)) if "0x" in v else Real.of(int(v))
            for k, v in f.items():
                if k.startswith("S:"):
                    k = k[2:]; state[k] = v if v in S["enums"] else Real.of(dbl(v))
                elif k == "NOW":
                    now = Real.of(int(v)); funcs["since"] = (lambda now: (lambda t: now - t))(now)
                elif v in ("true", "false"): state[k] = (v == "true")
            env = Env(th, funcs, state, S["enums"])
            try:
                adm = admitted(th, env, mode, None if ev == "none" else ev)
                verdict = "agree" if adm == [rec["next"]] else "disagree"
            except Undecided: adm, verdict = [], "undecided"
            dok = ""
            if verdict == "disagree":
                denv = as_double_env(env)
                if "since" in funcs: denv.funcs["since"] = (lambda n: (lambda x: n - float(x)))(float(now.q))
                dok = admitted(th, denv, mode, None if ev == "none" else ev) == [rec["next"]]
            rows.append(dict(set=kind, id=sid, mode=mode, event=ev, java=rec["next"], model="|".join(adm), verdict=verdict,
                             double_model_agrees=dok, inputs=" ".join(l.split()[3:])))
    os.makedirs(out_dir, exist_ok=True)
    with open(f"{out_dir}/{study}-reference.csv", "w", newline="") as fh:
        w = csv.DictWriter(fh, fieldnames=list(rows[0])); w.writeheader(); w.writerows(rows)
    return rows


if __name__ == "__main__":
    for st in sys.argv[4:] or list(SPECS):
        rows = run(st, *sys.argv[1:4])
        print(st, collections.Counter((r["set"], r["verdict"], r["double_model_agrees"]) for r in rows))
        print("  ", collections.Counter((r["mode"], r["java"], r["model"], r["inputs"]) for r in rows if r["verdict"] != "agree").most_common(12))
