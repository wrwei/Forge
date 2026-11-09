#!/usr/bin/env python3
"""Differential guard check, LRE: Java LreController.step vs the extracted theory."""
import csv, itertools, json, math, os, random, sys
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, HERE)
from common import *
from model_eval import as_double_env, refresh

MODES = ["OCM", "MOM", "HCM", "CAM"]
EVENTS = ["reqVel", "reqHdng", "reqOCM", "reqMOM", "reqHCM", "endTask", "none"]
up, dn = math.nextafter(1.0, 2), math.nextafter(1.0, 0)


def boundary():
    vels = [(0, 0, 0), (1, 0, 0), (0, 1, 0), (0, 0, 1), (0.6, 0.8, 0), (0.8, 0.6, 0), (0.28, 0.96, 0),
            (0, 0.6, 0.8), (0.6, 0, 0.8), (dn, 0, 0), (up, 0, 0), (0, 0, up), (0, 0, dn), (0.5, 0.5, 0.5),
            (0.7071067811865476, 0.7071067811865476, 0), (0.7071067811865475, 0.7071067811865475, 0),
            (0.6, 0.8, 1), (-0.6, 0.8, 0), (2, 0, 0), (0, 0, -1)]
    depths = [-1.0, 0.0, 5e-324, 1.0, 5.0, 0.1]
    def st(ns, ew, dz): return (ns, ew, dz, 0.0, 0.0, 0.0)
    statics = [[], [st(1, 0, 0)], [st(0.6, 0.8, 0)], [st(dn, 0, 0)], [st(up, 0, 0)], [st(5, 0, 1)],
               [st(5, 0, up)], [st(0.6, 0.8, 0.5), st(3, 4, 0)], [st(1, 0, 1)], [st(0, 0, 1)]]
    def dy(ns, ew, dz, vn, ve): return (ns, ew, dz, vn, ve, 0.0)
    dynamics = [[], [dy(2, 1, 0, -1, 0)], [dy(2, dn, 0, -1, 0)], [dy(2, up, 0, -1, 0)], [dy(2, 0.6, 0, -1, 0)],
                [dy(-2, 1, 0, 1, 0)], [dy(3, 4, 0, -0.6, -0.8)], [dy(2, 1, 0, 1e-300, 0)]]
    for v, d, s, y in itertools.product(vels, depths, statics, dynamics):
        # obstacle depths are relative offsets from the AUV's depth
        obs = [(o[0], o[1], d + o[2], o[3], o[4], o[5]) for o in s + y]
        yield (d, *v, obs)


def rand(n, seed=20261109):
    R = random.Random(seed)
    pick = lambda: R.choice([0.0, 1.0, -1.0, 0.5, 2.0, R.uniform(-2, 2), round(R.uniform(-2, 2), 1), up, dn])
    for _ in range(n):
        d = R.choice([-0.5, 0.0, 1.0, 5.0, R.uniform(-1, 6)])
        obs = []
        for _ in range(R.randint(0, 3)):
            dyn = R.random() < 0.5
            obs.append((pick() * 2, pick() * 2, d + pick(), pick() if dyn else 0.0, pick() if dyn else 0.0, 0.0))
        yield (d, pick(), pick(), pick(), obs)


def line(sid, mode, ev, sc, base=0):
    d, ns, ew, roc, obs = sc
    parts = [sid, mode, ev] + [repr(float(x)) for x in (d, ns, ew, roc)] + [str(len(obs))]
    for i, o in enumerate(obs): parts += [str(i + base)] + [repr(float(x)) for x in o]
    return " ".join(parts)


def model_env(th, rv, rec):
    f = rec["funcs"]; funcs = {}
    for k, v in f.items():
        if isinstance(v, dict):
            tab = {i: Real.of(dbl(x)) for i, x in v.items()}
            funcs[k] = (lambda tab: (lambda a: tab[int(Real.of(a).q)]))(tab)
        else:
            funcs[k] = Real.of(dbl(v)) if ("0x" in v or "." in v) else Real.of(int(v))
    env = Env(th, funcs, {})
    state = {k: env.eval(e) for k, e in rv.items()}
    denv = as_double_env(Env(th, funcs, {})); refresh(denv, rv)
    return (Env(th, funcs, state), denv), state


def main(thy, src, tag, out_dir):
    th = parse_theory(thy); rv = refreshed_vars(th)
    assert set(rv) == {"camActive", "cda", "cdyn", "cstc", "hcmActive", "hvel", "inOpez", "momReturn", "tcpa", "vel", "vvel"}, sorted(rv)
    B = f"/tmp/dgc/{tag}"; compile_harness(src, os.path.join(HERE, "harness/LreHarness.java"), B)
    rows = []
    for kind, gen in (("boundary", list(boundary())), ("random", list(rand(2000)))):
        lines = [line(f"{kind[0]}{i}_{m}_{e}", m, e, sc) for i, sc in enumerate(gen) for m in MODES for e in EVENTS]
        res = run_harness(B, "harness.LreHarness", lines)
        cache = {}
        for l in lines:
            sid, mode, ev = l.split()[:3]; rec = res[sid]; key = sid.split("_")[0]
            if key not in cache:
                try: cache[key] = model_env(th, rv, rec)
                except Undecided as u: cache[key] = u
            ce = cache[key]
            jf = rec["fields"]
            nonfinite = any(not math.isfinite(dbl(jf[k])) for k in ("hvel", "vvel", "vel", "cda", "tcpa"))
            if isinstance(ce, Undecided):
                rows.append(dict(set=kind, id=sid, mode=mode, event=ev, java=rec["next"], model="", verdict="undecided", java_nonfinite=nonfinite, flag_diffs="", cause="", double_model_agrees="", note=str(ce))); continue
            (env, denv), state = ce
            try:
                adm = admitted(th, env, mode, None if ev == "none" else ev)
                verdict = "agree" if adm == [rec["next"]] else "disagree"
            except Undecided as u:
                adm, verdict = [], "undecided"
            diffs = [k for k in ("inOpez", "camActive", "hcmActive", "momReturn") if (jf[k] == "true") != bool(state[k])]
            sides = []          # derived quantities whose double and exact values fall on different sides of a threshold
            if verdict == "disagree":
                for q, thr, rel in (("vel", 1, "le"), ("hvel", 1, "ge"), ("vvel", 1, "ge"), ("cda", 1, "lt"), ("tcpa", 0, "ge")):
                    if not math.isfinite(dbl(jf[q])):
                        sides.append(q + "=" + jf[q]); continue
                    jv, mv = Real.of(dbl(jf[q])), state[q]
                    try:
                        jt = {"le": jv <= thr, "ge": jv >= thr, "lt": jv < thr}[rel]; mt = {"le": mv <= thr, "ge": mv >= thr, "lt": mv < thr}[rel]
                    except Undecided: continue
                    if jt != mt: sides.append(q)
            for k in ("cstc", "cdyn"):
                if int(jf[k]) != int(state[k].q): diffs.append(k)
            rows.append(dict(set=kind, id=sid, mode=mode, event=ev, java=rec["next"], model="|".join(adm), verdict=verdict,
                             java_nonfinite=nonfinite, flag_diffs=";".join(diffs),
                             cause=("" if verdict == "agree" else ";".join(sides) if sides else "unexplained"),
                             double_model_agrees=("" if verdict == "agree" else admitted(th, denv, mode, None if ev == "none" else ev) == [rec["next"]]), note=""))
    os.makedirs(out_dir, exist_ok=True)
    with open(f"{out_dir}/{tag}.csv", "w", newline="") as fh:
        w = csv.DictWriter(fh, fieldnames=list(rows[0])); w.writeheader(); w.writerows(rows)
    return rows


if __name__ == "__main__":
    rows = main(*sys.argv[1:5])
    import collections
    print(collections.Counter((r["set"], r["verdict"]) for r in rows))
    print(collections.Counter((r["verdict"], r["double_model_agrees"], "nonfinite" if r["java_nonfinite"] else "finite") for r in rows))
