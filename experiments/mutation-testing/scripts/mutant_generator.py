#!/usr/bin/env python3
"""Mutation-testing experiment: mutant generator.

Enumerates injection sites for seven defect classes over the three case
studies' Java subjects (reference-runs/<study>/java, read-only), then
materializes one staged source tree per selected mutant under
<output>/mutants/<mutant_id>/java/.

Defect classes
  relop_flip     : >= -> <,  <= -> >,  > -> <=,  < -> >=   (guard sign flip)
  cmp_swap       : == <-> !=
  branch_swap    : exchange condition+body of two adjacent if/else-if branches
  sentinel_init  : initialiser -1 / -1.0 -> 0 / 0.0          (sentinel-initialiser class)
  trigger_swap   : instanceof <Event>.X -> instanceof <Event>.Y (C2-sensitive)
  off_by_one     : numeric threshold literal n -> n+1
  drop_negation  : !(...) -> (...),  !x -> x

Selection: ALL enumerable sites for branch_swap / sentinel_init /
trigger_swap / drop_negation; seeded sample (seed=20260815) capped at
CAP_PER_STUDY sites per study for relop_flip / cmp_swap / off_by_one.
Comment and string spans are masked out of enumeration.
"""
import csv
import os
import random
import re
import shutil
import sys
from pathlib import Path

SEED = 20260815
CAP_PER_STUDY = 10
CAPPED_CLASSES = {"relop_flip", "cmp_swap", "off_by_one"}

REF = Path(os.environ.get("FORGE_REFERENCE_RUNS", "reference-runs"))  # run from the repository root
STUDIES = ["lre", "sranger", "chemical_detector"]
STAGE = Path(os.environ.get("MUTANT_OUTPUT_DIR", "/tmp/mutation-testing/mutants"))


# ── comment/string masking ──────────────────────────────────────────────────
def code_mask(text: str):
    """Boolean list: True where the character is real code (not comment,
    string literal, or char literal)."""
    n = len(text)
    mask = [True] * n
    i = 0
    while i < n:
        c = text[i]
        if c == "/" and i + 1 < n and text[i + 1] == "/":
            j = text.find("\n", i)
            j = n if j == -1 else j
            for k in range(i, j):
                mask[k] = False
            i = j
        elif c == "/" and i + 1 < n and text[i + 1] == "*":
            j = text.find("*/", i + 2)
            j = n if j == -1 else j + 2
            for k in range(i, j):
                mask[k] = False
            i = j
        elif c == '"':
            j = i + 1
            while j < n and text[j] != '"':
                if text[j] == "\\":
                    j += 1
                j += 1
            for k in range(i, min(j + 1, n)):
                mask[k] = False
            i = j + 1
        elif c == "'":
            j = i + 1
            while j < n and text[j] != "'":
                if text[j] == "\\":
                    j += 1
                j += 1
            for k in range(i, min(j + 1, n)):
                mask[k] = False
            i = j + 1
        else:
            i += 1
    return mask


def line_of(text: str, pos: int) -> int:
    return text.count("\n", 0, pos) + 1


# ── per-class site enumeration ──────────────────────────────────────────────
RELOP_MAP = {">=": "<", "<=": ">", ">": "<=", "<": ">="}


def sites_relop(text, mask):
    out = []
    for m in re.finditer(r" (>=|<=|>|<) ", text):
        p = m.start(1)
        if not mask[p]:
            continue
        op = m.group(1)
        out.append((p, p + len(op), op, RELOP_MAP[op]))
    return out


def sites_cmp(text, mask):
    out = []
    for m in re.finditer(r" (==|!=) ", text):
        p = m.start(1)
        if not mask[p]:
            continue
        op = m.group(1)
        out.append((p, p + 2, op, "!=" if op == "==" else "=="))
    return out


def sites_sentinel(text, mask):
    out = []
    for m in re.finditer(r"=\s*(-1(?:\.0)?)\s*;", text):
        p = m.start(1)
        if not mask[p]:
            continue
        old = m.group(1)
        new = "0.0" if old == "-1.0" else "0"
        out.append((p, p + len(old), old, new))
    return out


def sites_trigger(text, mask, variants_by_type):
    out = []
    for m in re.finditer(r"instanceof\s+(\w+)\.(\w+)", text):
        p = m.start(2)
        if not mask[m.start()]:
            continue
        etype, var = m.group(1), m.group(2)
        pool = sorted(v for v in variants_by_type.get(etype, set()) if v != var)
        if not pool:
            continue
        # deterministic: successor in the sorted variant cycle
        allv = sorted(variants_by_type[etype])
        nxt = allv[(allv.index(var) + 1) % len(allv)]
        if nxt == var:
            continue
        out.append((p, p + len(var), var, nxt))
    return out


def _bump(num: str) -> str:
    if "." in num:
        return repr(float(num) + 1.0)
    return str(int(num) + 1)


def sites_offbyone(text, mask):
    out = []
    seen = set()
    # (a) literal on either side of a comparison
    for m in re.finditer(r"(>=|<=|>|<)\s*(\d+(?:\.\d+)?)", text):
        p = m.start(2)
        if not mask[p]:
            continue
        seen.add(p)
        out.append((p, p + len(m.group(2)), m.group(2), _bump(m.group(2))))
    # (b) numeric constant initialisers (threshold constants)
    for m in re.finditer(
        r"static\s+final\s+(?:double|int|long)\s+\w+\s*=\s*(\d+(?:\.\d+)?)\s*;", text
    ):
        p = m.start(1)
        if not mask[p] or p in seen:
            continue
        out.append((p, p + len(m.group(1)), m.group(1), _bump(m.group(1))))
    return out


def sites_negation(text, mask):
    out = []
    for m in re.finditer(r"!\s*(?=[A-Za-z_(])", text):
        p = m.start()
        if not mask[p]:
            continue
        if p + 1 < len(text) and text[p + 1] == "=":
            continue  # !=
        nxt = text[m.end():]
        if nxt.startswith("("):
            # match parens
            depth, j = 0, m.end()
            while j < len(text):
                if text[j] == "(":
                    depth += 1
                elif text[j] == ")":
                    depth -= 1
                    if depth == 0:
                        break
                j += 1
            snippet = text[p : j + 1]
            out.append((p, m.end(), snippet[:40], ""))  # drop just the '!' (+ws)
        else:
            m2 = re.match(r"[A-Za-z_][\w.]*(?:\(\))?", nxt)
            if not m2:
                continue
            out.append((p, m.end(), "!" + m2.group(0), ""))
    return out


# ── branch-swap enumeration (brace-matching chain parser) ───────────────────
def find_chains(text, mask):
    """Return list of chains; each chain is a list of (cond_span, body_span)."""
    chains = []
    for m in re.finditer(r"\bif\s*\(", text):
        p = m.start()
        if not mask[p]:
            continue
        # skip if this 'if' is itself an 'else if' (chain continuation)
        before = text[:p].rstrip()
        if before.endswith("else"):
            continue
        chain = []
        pos = p
        while True:
            mm = re.match(r"if\s*\(", text[pos:])
            if not mm:
                break
            copen = pos + mm.end() - 1
            depth, j = 0, copen
            while j < len(text):
                if text[j] == "(" and mask[j]:
                    depth += 1
                elif text[j] == ")" and mask[j]:
                    depth -= 1
                    if depth == 0:
                        break
                j += 1
            cclose = j
            k = cclose + 1
            while k < len(text) and text[k] in " \t\r\n":
                k += 1
            if k >= len(text) or text[k] != "{":
                break  # unbraced body: bail on this chain
            depth, j = 0, k
            while j < len(text):
                if text[j] == "{" and mask[j]:
                    depth += 1
                elif text[j] == "}" and mask[j]:
                    depth -= 1
                    if depth == 0:
                        break
                j += 1
            bclose = j
            chain.append(((copen, cclose + 1), (k, bclose + 1)))
            # look ahead for 'else if' / 'else {'
            k2 = bclose + 1
            while True:
                while k2 < len(text) and text[k2] in " \t\r\n":
                    k2 += 1
                if text[k2 : k2 + 2] == "//":
                    nl = text.find("\n", k2)
                    k2 = len(text) if nl == -1 else nl + 1
                    continue
                if text[k2 : k2 + 2] == "/*":
                    ce = text.find("*/", k2)
                    k2 = len(text) if ce == -1 else ce + 2
                    continue
                break
            if text[k2 : k2 + 4] == "else":
                k3 = k2 + 4
                while k3 < len(text) and text[k3] in " \t\r\n":
                    k3 += 1
                if text[k3 : k3 + 2] == "if":
                    pos = k3
                    continue
                else:
                    break  # bare else terminates chain
            break
        if len(chain) >= 2:
            chains.append(chain)
    return chains


def sites_branch_swap(text, mask):
    """One site per adjacent branch pair in each if/else-if chain."""
    out = []
    for chain in find_chains(text, mask):
        for i in range(len(chain) - 1):
            (c1, b1), (c2, b2) = chain[i], chain[i + 1]
            out.append((c1, b1, c2, b2))
    return out


def apply_branch_swap(text, site):
    (c1, b1, c2, b2) = site
    cond1, body1 = text[c1[0] : c1[1]], text[b1[0] : b1[1]]
    cond2, body2 = text[c2[0] : c2[1]], text[b2[0] : b2[1]]
    # spans ordered: c1 < b1 < c2 < b2
    return (
        text[: c1[0]] + cond2 + text[c1[1] : b1[0]] + body2 +
        text[b1[1] : c2[0]] + cond1 + text[c2[1] : b2[0]] + body1 +
        text[b2[1] :]
    )


# ── main enumeration ────────────────────────────────────────────────────────
def collect_event_variants(java_files):
    """Map event-type simple name -> set of variant names, from both
    instanceof usages and nested record/class declarations in *Event files."""
    variants = {}
    for f in java_files:
        text = f.read_text()
        for m in re.finditer(r"instanceof\s+(\w+)\.(\w+)", text):
            variants.setdefault(m.group(1), set()).add(m.group(2))
        if f.stem.endswith("Event"):
            for m in re.finditer(
                r"(?:record|final\s+class|class)\s+(\w+)[\s(<]", text
            ):
                if m.group(1) != f.stem:
                    variants.setdefault(f.stem, set()).add(m.group(1))
    return variants


def main():
    rng = random.Random(SEED)
    manifest = []
    STAGE.mkdir(parents=True, exist_ok=True)

    for study in STUDIES:
        jroot = REF / study / "java"
        java_files = sorted(jroot.rglob("*.java"))
        variants_by_type = collect_event_variants(java_files)

        # enumerate sites per class across all files (deterministic order)
        persite = {c: [] for c in [
            "relop_flip", "cmp_swap", "branch_swap", "sentinel_init",
            "trigger_swap", "off_by_one", "drop_negation"]}
        for f in java_files:
            text = f.read_text()
            mask = code_mask(text)
            rel = str(f.relative_to(jroot))
            for cls, sites in [
                ("relop_flip", sites_relop(text, mask)),
                ("cmp_swap", sites_cmp(text, mask)),
                ("sentinel_init", sites_sentinel(text, mask)),
                ("trigger_swap", sites_trigger(text, mask, variants_by_type)),
                ("off_by_one", sites_offbyone(text, mask)),
                ("drop_negation", sites_negation(text, mask)),
            ]:
                for s in sites:
                    persite[cls].append((rel, s))
            for s in sites_branch_swap(text, mask):
                persite["branch_swap"].append((rel, s))

        # seeded sampling for capped classes
        for cls, entries in persite.items():
            chosen = entries
            if cls in CAPPED_CLASSES and len(entries) > CAP_PER_STUDY:
                chosen = sorted(rng.sample(range(len(entries)), CAP_PER_STUDY))
                chosen = [entries[i] for i in chosen]
            for idx, (rel, site) in enumerate(chosen):
                mid = f"{study}_{cls}_{idx:03d}"
                src = jroot / rel
                text = src.read_text()
                if cls == "branch_swap":
                    new_text = apply_branch_swap(text, site)
                    line = line_of(text, site[0][0])
                    before = text[site[0][0] : site[0][1]][:60]
                    after = text[site[2][0] : site[2][1]][:60]
                else:
                    (a, b, old, new) = site
                    new_text = text[:a] + new + text[b:]
                    line = line_of(text, a)
                    ls = text.rfind("\n", 0, a) + 1
                    le = text.find("\n", a)
                    before = text[ls:le].strip()[:80]
                    nle = new_text.find("\n", ls)
                    after = new_text[ls:nle].strip()[:80]
                # stage mutant tree
                mdir = STAGE / mid / "java"
                if mdir.exists():
                    shutil.rmtree(mdir)
                shutil.copytree(jroot, mdir)
                (mdir / rel).write_text(new_text)
                manifest.append(dict(
                    mutant_id=mid, study=study, defect_class=cls,
                    file=rel, line=line, before=before, after=after,
                    enumerated=len(entries), selected=len(chosen),
                ))
        counts = {c: (len(persite[c]),
                      min(len(persite[c]), CAP_PER_STUDY) if c in CAPPED_CLASSES
                      else len(persite[c])) for c in persite}
        print(study, counts)

    out = Path("mutants_manifest.csv")
    with out.open("w", newline="") as fh:
        w = csv.DictWriter(fh, fieldnames=list(manifest[0].keys()))
        w.writeheader()
        w.writerows(manifest)
    print(f"total mutants staged: {len(manifest)}")


if __name__ == "__main__":
    main()
