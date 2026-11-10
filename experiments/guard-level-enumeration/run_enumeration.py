"""Guard-level conformance enumeration for the three RoboStar case studies (four controllers).

For every mode, truth value of each named guard predicate and input event, compare the next mode
the Java controller takes with the set of target modes the extracted Isabelle theory admits.

Usage (from the repository root):
  bash scripts/generate_isabelle.sh <dir>        # extract the theories from reference-runs/<study>/java
  python experiments/guard-level-enumeration/run_enumeration.py --thy-root <dir>

Writes enumeration_rows.csv and prints the agreement per controller.
"""
import argparse, csv, glob, itertools, os, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import enumeration_lib as lib
import enumeration_studies as cfgs

REPO = os.path.abspath(os.path.join(HERE, "..", ".."))

def java_next(cfg, jmodes, mode, g, ev):
    if cfg["java_kind"] == "lre":
        return lib.lre_java_next(jmodes, mode, g, ev, cfgs.LRE_PREDS)
    if cfg["java_kind"] == "sranger":
        return lib.sr_java_next(mode, g, ev)
    if cfg["java_kind"] == "movement":
        return lib.mv_java_next(mode, g, ev)
    return lib.cd_java_next(mode, g, ev)

def enumerate_states(thy_root, java_root):
    rows, unknown_all = [], {}
    for study, cfg in cfgs.STUDIES.items():
        sdir = cfg.get("dir", study)
        hits = glob.glob(os.path.join(thy_root, sdir, "**", cfg["thy"]), recursive=True)
        if not hits:
            sys.exit(f"no {cfg['thy']} under {os.path.join(thy_root, sdir)}")
        ops, listens = lib.parse_thy(hits[0])
        jp = os.path.join(java_root, sdir, cfg["java_rel"])
        jmodes = lib.lre_java_chains(jp) if cfg["java_kind"] == "lre" else None
        unknown, gnames = set(), list(cfg["gvars"])
        for mode in cfg["modes"]:
            for vals in itertools.product(*[cfg["gvars"][k] for k in gnames]):
                g = dict(zip(gnames, vals))
                for ev in cfg["events"]:
                    jn = java_next(cfg, jmodes, mode, g, ev)
                    adm = lib.model_admits(ops, listens, mode, g, cfg["ev_thy"][ev], cfg, unknown)
                    tg = sorted({o["target"] for o in adm}) or [mode]
                    r = dict(study=study, mode=mode, event=ev)
                    r.update(g)
                    r.update(java_next=jn, model_admits="|".join(tg), n_adm=len(adm),
                             model_ops="|".join(o["name"] for o in adm) or "-", agree=(tg == [jn]))
                    rows.append(r)
        unknown_all[study] = sorted(unknown)
    return rows, unknown_all

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--thy-root", required=True, help="directory of extracted theories, one subdirectory per study")
    ap.add_argument("--java-root", default=os.path.join(REPO, "reference-runs"))
    ap.add_argument("--out", default=None)
    a = ap.parse_args()
    rows, unknown = enumerate_states(a.thy_root, a.java_root)
    out = a.out or "enumeration_rows.csv"
    cols = []
    for r in rows:                      # first-seen order: deterministic across runs
        cols += [k for k in r if k not in cols]
    with open(out, "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=cols); w.writeheader(); w.writerows(rows)
    for study in cfgs.STUDIES:
        rs = [r for r in rows if r["study"] == study]
        ag = sum(r["agree"] for r in rs)
        print(f"{study:18s} states={len(rs):5d} agree={ag:5d} ({100*ag/len(rs):.1f}%)"
              + (f"  unparsed atoms: {unknown[study]}" if unknown[study] else ""))
    print(f"rows written to {out}")

if __name__ == "__main__":
    main()
