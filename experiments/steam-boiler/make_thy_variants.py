#!/usr/bin/env python3
"""make_thy_variants.py v2 — isolation ladder for a generated *_Beh.thy.

Campaign rule: an anonymous timeout is never evidence. v2 ladder (F9):

  Skeleton — zstore + records + ALL zoperations + zmachine, ZERO lemmas.
             Times out => the cost is upstream of proofs (record simp setup /
             machine elaboration) and lemma bisection is moot.
  OneLemma — Skeleton + ONE representative inv-preservation lemma
             (the first zoperation's), to price a single zpog_full proof.
  NoDlf    — deadlock_free removed, all other lemmas kept (v1).
  HoareA/B — NoDlf with first/second half of preservation lemmas (v1).

Writes one directory per variant plus a ROOT with per-dir sessions
(`session X in "DIR" = "Z_Machines" + ...`), matching the user's restructured
layout, so one `isabelle build -D <out>` covers any subset.

Usage: make_thy_variants.py <BoilerController_Beh.thy> <outdir>
"""
import re
import sys
from pathlib import Path

def main(src_path, outdir):
    src = Path(src_path).read_text()
    base = re.match(r"theory (\w+)", src).group(1)
    stm = base.removesuffix("_Beh")

    # --- deadlock_free block (lemma up to final `end`) ---
    dlf = re.search(r"\nlemma \w+_deadlock_free:.*?(?=\nend\s*$)", src, re.S)
    assert dlf, "deadlock_free lemma not found"
    nodlf = src[:dlf.start()] + "\n(* isolation: deadlock_free lemma removed *)\n" + src[dlf.end():]

    # --- preservation/establishes lemma blocks ---
    lem_pat = re.compile(r"\nlemma [^\n]*(?:preserves|establishes)[^\n]*\n(?:.*?\n)*?\s*by[^\n]*\n")
    lems = list(lem_pat.finditer(nodlf))
    assert lems, "no preservation lemmas found"
    half = len(lems) // 2

    def drop(text, matches, note):
        out = text
        for mm in reversed(matches):
            out = out[:mm.start()] + "\n" + out[mm.end():]
        return out.replace("begin\n", "begin\n(* isolation: " + note + " *)\n", 1)

    skeleton = drop(nodlf, lems, "ZERO lemmas - elaboration-only skeleton (zstore+records+zops+zmachine)")
    # OneLemma: keep exactly the first inv-preservation lemma (skip Init establishes)
    inv_lems = [m for m in lems if "preserves" in m.group(0)]
    keep = inv_lems[0]
    onelemma = drop(nodlf, [m for m in lems if m.span() != keep.span()],
                    "ONE representative inv-preservation lemma kept: " +
                    re.search(r"lemma (\w+)", keep.group(0)).group(1))
    hoareA = drop(nodlf, lems[half:], "first half of preservation lemmas (%d/%d)" % (half, len(lems)))
    hoareB = drop(nodlf, lems[:half], "second half of preservation lemmas (%d/%d)" % (len(lems)-half, len(lems)))

    out = Path(outdir)
    variants = [("Skeleton", skeleton), ("OneLemma", onelemma),
                ("NoDlf", nodlf), ("HoareA", hoareA), ("HoareB", hoareB)]
    roots = []
    for suffix, text in variants:
        name = base + "_" + suffix
        d = out / suffix
        d.mkdir(parents=True, exist_ok=True)
        (d / (name + ".thy")).write_text(text.replace("theory " + base, "theory " + name, 1))
        roots.append('session %s_%s_Check in "%s" = "Z_Machines" +\n  theories\n    %s\n'
                     % (stm, suffix, suffix, name))
    (out / "ROOT").write_text("\n".join(roots))
    print("variants:", ", ".join(v[0] for v in variants),
          "| preservation lemmas:", len(lems), "| kept in OneLemma:",
          re.search(r"lemma (\w+)", keep.group(0)).group(1))

if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
