#!/usr/bin/env python3
"""Confirm each mutant preserves behaviour: run it and its original through the differential
check's harnesses on the same inputs and compare the next mode at every step."""
import csv, os, random, shutil, sys
HERE = os.path.dirname(os.path.abspath(__file__))
DG = os.path.join(HERE, "..", "differential-guard-check"); sys.path.insert(0, DG)
from common import compile_harness, run_harness
import run_lre as L, run_small as S
REF = os.path.join(HERE, "..", "..", "reference-runs")


def lines_for(kind):
    if kind == "lre":
        out = []
        for k, gen in (("b", list(L.boundary())), ("r", list(L.rand(2000)))):
            out += [L.line(f"{k}{i}_{m}_{e}", m, e, sc) for i, sc in enumerate(gen) for m in L.MODES for e in L.EVENTS]
        return out
    b, r, fmt = S.scenarios(kind, random.Random(20261109)); sp = S.SPECS[kind]; out = []
    for k, gen in (("b", b), ("r", r)):
        out += [f"{k}{i}_{m}_{e} {m} {e} {fmt(sc)}" for i, sc in enumerate(gen) for m in sp["modes"] for e in sp["events"]]
    return out


HARN = {"lre": ("LreHarness", ["lre"]), "sranger": ("SRangerHarness", ["sranger"]), "chemical_detector": ("GasHarness+MovementHarness", ["gas", "movement"])}


def harness_src(name, rename=None):
    t = open(os.path.join(DG, "harness", name + ".java")).read()
    if rename: t = t.replace(*rename)
    d = f"/tmp/bpb/src-{name}-{abs(hash(rename))}"; os.makedirs(d, exist_ok=True)
    p = os.path.join(d, name + ".java"); open(p, "w").write(t); return p


def modes(src_java, kind, rename=None):
    name = {"lre": "LreHarness", "sranger": "SRangerHarness", "gas": "GasHarness", "movement": "MovementHarness"}[kind]
    B = f"/tmp/bpb/{abs(hash((src_java, kind, rename)))}"; shutil.rmtree(B, ignore_errors=True)
    compile_harness(src_java, [harness_src(name, rename), os.path.join(DG, "harness", "Refl.java")], B)
    res = run_harness(B, f"harness.{name}", lines_for(kind))
    return {k: v["next"] for k, v in res.items()}


if __name__ == "__main__":
    man = list(csv.DictReader(open(os.path.join(HERE, "mutants_manifest.csv"))))
    base = {}
    rows = []
    for r in man:
        st = r["study"]
        kinds = ["lre"] if st == "lre" else ["sranger"] if st == "sranger" else (["gas"] if "gasanalysis" in r["files"] else ["movement"] if "movement/" in r["files"] else ["gas", "movement"])
        for k in kinds:
            if k not in base: base[k] = modes(os.path.join(REF, st, "java"), k)
            rename = ('"turnDurationElapsed"', '"turnDone"') if r["mutant_id"] == "sranger_bp_rename_02" else None
            got = modes(os.path.join(HERE, "mutants", r["mutant_id"], "java"), k, rename)
            diff = sum(1 for x in base[k] if got.get(x) != base[k][x])
            rows.append(dict(mutant_id=r["mutant_id"], controller=k, steps=len(base[k]), differing_steps=diff))
            print(r["mutant_id"], k, len(base[k]), diff, flush=True)
    with open(os.path.join(HERE, "behaviour_check.csv"), "w", newline="") as fh:
        w = csv.DictWriter(fh, fieldnames=list(rows[0])); w.writeheader(); w.writerows(rows)
