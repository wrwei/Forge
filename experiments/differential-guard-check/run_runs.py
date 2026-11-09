#!/usr/bin/env python3
"""Differential guard check on the converged code of the LRE loop runs (secondary)."""
import collections, csv, glob, math, os, re, sys
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(HERE, "..", "requirements-constants-check"))
from common import *
from model_eval import Real, Env, parse_theory, refreshed_vars, admitted, Undecided, refresh, as_double_env
from check_stated_values import java_sources, declared_constants, norm
import run_lre as L


def converged(run):
    its = [p for p in glob.glob(f"{run}/iter-*") if re.search(r"iter-\d+$", p)]
    return max(its, key=lambda p: int(p.rsplit("-", 1)[1]))


def funcs_from(rec, consts):
    funcs = {}
    for k, v in rec["funcs"].items():
        if isinstance(v, dict):
            tab = {i: Real.of(dbl(x)) for i, x in v.items()}
            funcs[k] = (lambda tab: (lambda a: tab[int(Real.of(a).q) if isinstance(a, Real) else int(float(a))]))(tab)
        else: funcs[k] = Real.of(dbl(v)) if "0x" in v else Real.of(int(v))
    funcs["depth_fn"] = funcs.get("depth")
    for n, vals in consts.items(): funcs.setdefault(n, Real.of(vals[0][1]))
    return funcs


class CEnv(Env):
    """Uninterpreted constants of the theory take the Java's declared values (names compared
    ignoring case and underscores)."""
    def CALL(self, name):
        if name in self.th["defs"] or name in self.funcs: return super().CALL(name)
        if norm(name) in self.funcs: return self.funcs[norm(name)]
        raise KeyError(name)


def run(run_dir, tag, out_dir):
    it = converged(run_dir); thy = glob.glob(f"{it}/formal-artefacts/isabelle/*_Beh.thy")[0]
    th = parse_theory(thy); rv = refreshed_vars(th)
    consts = {norm(k): v for k, v in declared_constants(java_sources(f"{it}/java")).items()}
    B = f"/tmp/dgc/{tag}"
    import subprocess
    os.makedirs(B, exist_ok=True)
    files = [os.path.join(d, f) for d, _, fs in os.walk(f"{it}/java") for f in fs if f.endswith(".java") and "/app/" not in d]
    r = subprocess.run([f"{JAVA}/javac", "-parameters", "-d", B] + files + [f"{HERE}/harness/Refl.java", f"{HERE}/harness/GenericLreHarness.java"], capture_output=True, text=True)
    if r.returncode: raise RuntimeError(r.stderr[-2000:])
    rows = []
    for kind, gen in (("boundary", list(L.boundary())), ("random", list(L.rand(2000)))):
        lines = [L.line(f"{kind[0]}{i}_{m}_{e}", m, e, sc, base=1) for i, sc in enumerate(gen) for m in L.MODES for e in L.EVENTS]
        res = run_harness(B, "harness.GenericLreHarness", lines)
        cache = {}
        for l in lines:
            sid, mode, ev = l.split()[:3]; rec = res[sid]; key = sid.split("_")[0]
            if key not in cache:
                env = CEnv(th, funcs_from(rec, consts), {})
                try:
                    refresh(env, rv); denv = as_double_env(env); denv.__class__ = CEnv; refresh(denv, rv); cache[key] = (env, denv)
                except Undecided as u: cache[key] = u
            ce = cache[key]
            if isinstance(ce, Undecided):
                rows.append(dict(run=tag, set=kind, id=sid, mode=mode, event=ev, java=rec["next"], model="", verdict="undecided", cause="")); continue
            env, denv = ce
            try:
                adm = admitted(th, env, mode, None if ev == "none" else ev)
                verdict = "agree" if adm == [rec["next"]] else "disagree"
            except Undecided: adm, verdict = [], "undecided"
            cause = ""
            if verdict == "disagree":
                dadm = admitted(th, denv, mode, None if ev == "none" else ev)
                nonfin = any(isinstance(v, float) and not math.isfinite(v) for v in denv.state.values())
                cause = ("double-arithmetic" + ("(non-finite)" if nonfin else "(finite)")) if dadm == [rec["next"]] else "unexplained"
            rows.append(dict(run=tag, set=kind, id=sid, mode=mode, event=ev, java=rec["next"], model="|".join(adm), verdict=verdict, cause=cause))
    os.makedirs(out_dir, exist_ok=True)
    with open(f"{out_dir}/{tag}.csv", "w", newline="") as fh:
        w = csv.DictWriter(fh, fieldnames=list(rows[0])); w.writeheader(); w.writerows(rows)
    return rows


if __name__ == "__main__":
    X, out = sys.argv[1], sys.argv[2]
    for r in sys.argv[3:]:
        rows = run(f"{X}/lre/run-{r}", f"lre-run{r}", out)
        print(f"lre run-{r}", collections.Counter((x["verdict"], x["cause"]) for x in rows))
