#!/usr/bin/env python3
"""Refuse instruction text that carries any evaluation study's content.

    python3 scripts/instruction_leak_gate.py <worktree-or-repo-root> [--feedback] [--list]

Checks every file a generation session reads (CLAUDE.md, forge.assets/prompts/*,
forge.assets/vibe-coding-prompts/*) and, with --feedback, the message text the
pipeline writes into its feedback reports (string literals in the feedback,
vacuity and correction modules and the structural linter). A line fails when it
contains, for any of the four studies (SRanger, LRE, Chemical Detector, Steam
Boiler):

  1. the study's name or requirement-ID prefix;
  2. an identifier from the study's requirements, system description or
     reference-run Java (exact, case-sensitive; identifier-shaped tokens only);
  3. one of the study's domain words (DOMAIN below, whole words, any case).

DOMAIN was chosen by hand from the words that occur at least three times in one
study's requirements and in no other study's (29 October 2026), keeping only
words that name the study's domain, not general English or programming words.
Exit status 0 when clean, 1 when any line fails (each failure is printed).
"""
import ast, glob, os, re, sys

_WORDS = set(w.strip().lower() for w in open('/usr/share/dict/words')) if os.path.exists('/usr/share/dict/words') else set()

FORGE = os.environ.get("FORGE_ROOT", "/Users/ranwei/Git/Forge")

NAMES = {
    "sranger": r"\bS-?Ranger\b|\bsranger\b|\bSR-[A-Z]{2}",
    "lre": r"\bLRE\b|\blre\b|\bLRE-[A-Z]{2}|\bLRE_|_LRE\b|\blre_|_lre\b|\bLre[A-Z_]|\bLre\b",
    "chemical_detector": r"\bchemical\b|\bchem\b|\bchemical_detector\b|chem-?detector|chemicaldetector|\bgas ?analysis\b|\bCD-[A-Z]{2}",
    "steam_boiler": r"\bsteam\b|\bboiler\b|steam-?boiler|\bSB-[A-Z]{2}",
}
DOMAIN = {
    "sranger": "angular turnvel turnduration obstaclethreshold movevel motor motors infrared odometer odometers sonar rover",
    "lre": "auv autopilot heading cpa tcpa cda opez inopez cstc cdyn ocm hcm cam collision collisions underwater depth "
           "hdist vdist odist vvel hvel advvel advhdng minsafedist proximity",
    "chemical_detector": "gas gases intensity gasdetected nogas gassensor stuck stuckdist stuckperiod randomwalk evasion "
                         "avoiding avoidingagain gettingout tryingagain changedirection detector odour plume",
    "steam_boiler": "water pump pumps steam boiler valve litre litres evacuation rescue degraded transmission plant",
}
SHARED_DOMAIN = "obstacle obstacles"   # named by two of the studies' requirements
GENERIC_IDS = set("""RoboChart RoboChartType RoboChartWait Dafny Isabelle FDR4 Java CSP currentMode InputEvent OutputEvent
ElementType RetentionPolicy FIELD PARAMETER METHOD TYPE_USE SOURCE FINAL INITIAL Ctrl_State deadlock_free z_machine
toString hashCode valueOf ordinal NONE TRUE FALSE
SensorService RoboChartClock""".split())   # pipeline-defined conventions (recognised by the extractor)
GENERIC_IDS |= set("isStatic isFinal isEmpty keySet".split())   # ordinary programming names a reference run also declares


def _inputs(study):
    if study == "steam_boiler":
        return [f"{FORGE}/experiments/steam-boiler/sb_requirement_all.txt"]
    return (glob.glob(f"{FORGE}/forge.assets/case-studies/{study}/**/*.txt", recursive=True)
            + glob.glob(f"{FORGE}/reference-runs/{study}/java/**/*.java", recursive=True))


def _idents(text):
    text = re.sub(r"//[^\n]*|/\*.*?\*/", " ", text, flags=re.S)
    # identifier-shaped: camelCase, snake_case, with digits, or an ALL-CAPS token that is not an English word
    return {t for t in re.findall(r"\b[A-Za-z][A-Za-z0-9_]{3,}\b", text)
            if re.search(r"[a-z][A-Z]|_|[A-Za-z]\d", t) or (t.isupper() and len(t) >= 4 and t.lower() not in _WORDS)}


_DECL = re.compile(r"\b(?:class|interface|enum|record)\s+([A-Za-z_]\w*)"                       # type names
                   r"|[\w>\]]\s+([A-Za-z_]\w*)\s*(?:=|;|\(|,|\))"                              # fields, locals, methods, params
                   r"|^\s*([A-Z][A-Z0-9_]+)\s*[,;({]", re.M)                                       # enum constants


def _declared(java):
    """Names the study's Java DECLARES (types, methods, fields, variables, enum constants), not names it uses."""
    java = re.sub(r"//[^\n]*|/\*.*?\*/", " ", java, flags=re.S)
    java = re.sub(r'"(?:[^"\\]|\\.)*"', '""', java)
    out = set()
    for m in _DECL.finditer(java):
        t = next(g for g in m.groups() if g)
        if len(t) >= 4: out.add(t)
    return out


def vocabulary():
    v = {}
    for s in NAMES:
        ps = _inputs(s)
        req = "\n".join(open(p, errors="ignore").read() for p in ps if not p.endswith(".java"))
        java = "\n".join(open(p, errors="ignore").read() for p in ps if p.endswith(".java"))
        ids = {t for t in (_idents(req) | _declared(java)) if re.search(r"[a-z][A-Z]|_|[A-Za-z]\d", t) or (t.isupper() and t.lower() not in _WORDS)}
        v[s] = (ids - GENERIC_IDS, set(DOMAIN[s].split()))
    v["shared"] = (set(), set(SHARED_DOMAIN.split()))
    return v


def check_line(line, v):
    hits = []
    for s, pat in NAMES.items():
        m = re.search(pat, line, 0 if s == "lre" else re.I)
        if m:
            hits.append(f"{s}: name '{m.group(0)}'")
    toks = set(re.findall(r"\b[A-Za-z][A-Za-z0-9_]{3,}\b", line))
    words = set(re.findall(r"\b[a-z]+\b", line.lower()))
    for s, (ids, dom) in v.items():
        for t in sorted(toks & ids):
            hits.append(f"{s}: identifier '{t}'")
        for t in sorted(words & dom):
            hits.append(f"{s}: domain word '{t}'")
    return hits


def instruction_files(root):
    fs = [f"{root}/CLAUDE.md"] + sorted(glob.glob(f"{root}/forge.assets/prompts/*")) + sorted(glob.glob(f"{root}/forge.assets/vibe-coding-prompts/*"))
    fs += sorted(glob.glob(f"{root}/docs/corpus/**/*.md", recursive=True))   # the corpus's prose (its .thy files are the Z-Machine library)
    # the Gradle scaffold every session reads (its non-source, non-binary files) and pipeline.yaml
    fs += [f for f in sorted(glob.glob(f"{root}/java.generated.project/*") + glob.glob(f"{root}/java.generated.project/gradle/wrapper/*"))
           if os.path.isfile(f) and not f.endswith(".jar") and os.path.basename(f) != "result_codegen.json"]
    fs += [f"{root}/pipeline.yaml", f"{root}/.gitignore"]   # Amendments 7 and 8
    fs += sorted(f for f in glob.glob(f"{root}/forge.assets/corrections/*") if os.path.isfile(f))   # Amendment 9
    return [f for f in fs if os.path.isfile(f)]


# Generators whose output text (comments and warnings in generated artefacts) the developer can read.
EMITTING_TEMPLATES = "forge.transformations/src/main/resources/transformations/*.egl"

FEEDBACK_SOURCES = ["forge.dashboard/web/feedback/*.py", "forge.dashboard/web/vacuity.py", "forge.dashboard/web/csp_corrections.py",
                    "forge.dashboard/web/runners.py",
                    "forge.transformations/src/main/java/forge/transformations/preflight/StructuralLinter.java"]


def feedback_strings(path):
    """(line number, text) of each string literal that can reach a feedback report.

    Comments and docstrings are not shown to the developer and are skipped:
    Python is parsed (every str constant except module/class/function
    docstrings); Java has its comments blanked before string literals are read.
    """
    src = open(path, errors="ignore").read()
    out = []
    if path.endswith(".py"):
        tree = ast.parse(src)
        docs = set()
        for node in ast.walk(tree):
            if isinstance(node, (ast.Module, ast.ClassDef, ast.FunctionDef, ast.AsyncFunctionDef)) and node.body:
                b = node.body[0]
                if isinstance(b, ast.Expr) and isinstance(getattr(b, "value", None), ast.Constant) and isinstance(b.value.value, str):
                    docs.add(id(b.value))
        for node in ast.walk(tree):
            if isinstance(node, ast.Constant) and isinstance(node.value, str) and id(node) not in docs:
                for k, part in enumerate(node.value.split("\n")):
                    if part.strip():
                        out.append((node.lineno + k, part))
        return out
    src = re.sub(r"/\*.*?\*/", lambda m: re.sub(r"[^\n]", " ", m.group(0)), src, flags=re.S)
    src = re.sub(r"//[^\n]*", "", src)
    for m in re.finditer(r'"""(.*?)"""|\'\'\'(.*?)\'\'\'|"((?:[^"\\\n]|\\.)*)"|\'((?:[^\'\\\n]|\\.)*)\'', src, re.S):
        body = next(g for g in m.groups() if g is not None)
        line0 = src.count("\n", 0, m.start()) + 1
        for k, part in enumerate(body.split("\n")):
            if part.strip():
                out.append((line0 + k, part))
    return out


def main(argv):
    if len(argv) < 2:
        print(__doc__, file=sys.stderr); return 2
    root = argv[1].rstrip("/"); bad = 0; per = {}
    # A tree without the instruction files is an error, never "clean".
    if not os.path.isfile(f"{root}/CLAUDE.md") or not glob.glob(f"{root}/forge.assets/prompts/*"):
        print(f"ERROR: {root} has no CLAUDE.md or forge.assets/prompts/ -- not a pipeline tree", file=sys.stderr); return 2
    v = vocabulary()
    if "--no-archive" in argv and os.path.exists(f"{root}/docs/archive"):
        print(f"LEAK {root}/docs/archive is present (it holds models of evaluation studies)"); bad += 1
    if any(len(v[s][0]) == 0 for s in NAMES):
        print(f"ERROR: empty vocabulary for {[s for s in NAMES if not v[s][0]]} -- check FORGE_ROOT={FORGE}", file=sys.stderr); return 2
    for f in instruction_files(root):
        for i, line in enumerate(open(f, errors="ignore"), 1):
            if os.path.basename(f) == "pipeline.yaml" and line.strip().startswith("active_case_study:"):
                continue   # the session's own study setting (Amendment 8: comments are scanned)
            h = check_line(line, v)
            if h:
                bad += 1; per[f] = per.get(f, 0) + 1
                if "--list" in argv or "--summary" not in argv:
                    print(f"LEAK {os.path.relpath(f, root)}:{i}: {'; '.join(h[:4])} | {line.strip()[:120]}")
    if "--feedback" in argv:
        for pat in FEEDBACK_SOURCES:
            for f in sorted(glob.glob(f"{root}/{pat}")):
                for i, s in feedback_strings(f):
                    h = check_line(s, v)
                    if h:
                        bad += 1; per[f] = per.get(f, 0) + 1
                        if "--list" in argv or "--summary" not in argv:
                            print(f"LEAK {os.path.relpath(f, root)}:{i}: {'; '.join(h[:4])} | {s.strip()[:120]}")
    if "--feedback" in argv:
        for f in sorted(glob.glob(f"{root}/{EMITTING_TEMPLATES}")):
            src = open(f, errors="ignore").read()
            # blank code-block comments (// and /* */ inside [% %]); everything else may be emitted
            src = re.sub(r"/\*.*?\*/", lambda m: re.sub(r"[^\n]", " ", m.group(0)), src, flags=re.S)
            for i, line in enumerate(src.split("\n"), 1):
                line = re.sub(r"//.*", "", line)   # comments in template code; emitted Isabelle/CSP text does not use //
                h = [x for x in check_line(line, v) if "domain word" not in x]   # template code is full of generic words
                if h:
                    bad += 1; per[f] = per.get(f, 0) + 1
                    if "--list" in argv or "--summary" not in argv:
                        print(f"LEAK {os.path.relpath(f, root)}:{i}: {'; '.join(h[:4])} | {line.strip()[:120]}")
    for f, n in sorted(per.items()):
        print(f"  {os.path.relpath(f, root)}: {n} line(s)", file=sys.stderr)
    print(f"{'CLEAN' if bad == 0 else 'LEAKAGE'}: {bad} line(s) carry study content", file=sys.stderr)
    return 0 if bad == 0 else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv))
