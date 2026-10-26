#!/usr/bin/env python3
"""Regenerate the mutation campaign's artefacts with the published extractor and run the Dafny column.

For the three unmutated subjects (reference-runs/<study>/java) and the 186 mutants
(mutants/<id>/java): T2M -> M2M -> Dafny -> RCT -> Isabelle
through the campaign driver, then the vendored RoboChart CSP generator on the RCT, then
`dafny verify` (120 s cap, as the pipeline runs it). Writes outputs/<id>/... and outputs/results.csv with the artefact-changed flags (comment-stripped text comparison against the
study's baseline) and the Dafny verdict. Resumable: an id with a complete record is skipped.
"""
import csv, glob, json, os, re, shutil, subprocess, sys, time
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

HERE = Path(__file__).resolve().parent
M = HERE.parent                                  # experiments/mutation-testing
F = M.parent.parent                              # repository root
OUT = Path(os.environ.get("MUTGEN_OUT", M / "outputs"))
CLASSES = Path(os.environ.get("MUTGEN_CLASSES", M / "driver" / "classes"))
JAVA = Path(os.environ.get("JAVA_HOME", "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home")) / "bin/java"
DAFNY = os.environ.get("DAFNY", "/opt/homebrew/bin/dafny")
PAR = int(os.environ.get("MUTGEN_PAR", "4"))
cache = Path.home() / ".gradle/caches/modules-2/files-2.1"
CP = ":".join(str(p) for p in cache.rglob("*.jar") if "-sources" not in p.name and "-javadoc" not in p.name and not re.search(r"epsilon.*2\.5\.0", str(p)))
GCP = ":".join(sorted(str(p) for p in (F / "forge.transformations/lib/robochart-csp-gen").glob("*.jar")))
EXTRACTOR = subprocess.run(["git", "-C", str(F), "rev-parse", "--short", "HEAD"], capture_output=True, text=True,
                           env={**os.environ, "GIT_CONFIG_GLOBAL": "/dev/null", "GIT_CONFIG_SYSTEM": "/dev/null"}).stdout.strip()

def ensure_driver_classes():
    """Compile driver/CampaignDriver.java against the built extractor if driver/classes is missing."""
    if (CLASSES / "CampaignDriver.class").exists(): return
    CLASSES.mkdir(parents=True, exist_ok=True)
    javac = JAVA.parent / "javac"
    built = F / "forge.transformations/build/classes/java/main"
    if not built.exists(): sys.exit("build the extractor first: (cd forge.transformations && ./gradlew classes)")
    p = subprocess.run([str(javac), "-d", str(CLASSES), "-cp", f"{CP}:{built}", str(M / "driver" / "CampaignDriver.java")], capture_output=True, text=True)
    if p.returncode: sys.exit("javac failed:\n" + p.stderr[-2000:])
    # the driver needs the extractor's classes on the classpath at run time too
    for cls in built.rglob("*.class"):
        dst = CLASSES / cls.relative_to(built); dst.parent.mkdir(parents=True, exist_ok=True); shutil.copy(cls, dst)

def strip(t):  # drop comments and normalise whitespace for comparison
    t = re.sub(r"\(\*.*?\*\)", "", t, flags=re.S)          # Isabelle
    t = re.sub(r"//[^\n]*", "", t)                          # Dafny / RCT line comments
    t = re.sub(r"/\*.*?\*/", "", t, flags=re.S)
    t = re.sub(r"--[^\n]*", "", t)                          # CSP
    return " ".join(t.split())

def artefacts(d):
    d = Path(d)
    thy = {p.name: strip(p.read_text(errors="ignore")) for p in sorted((d / "isabelle").glob("*_Beh.thy"))}
    dfy = {p.name: strip(p.read_text(errors="ignore")) for p in sorted(d.glob("*.dfy"))}
    rct = {p.name: strip(p.read_text(errors="ignore")) for p in sorted(d.glob("*.rct"))}
    csp = {str(p.relative_to(d)): strip(p.read_text(errors="ignore")) for p in sorted((d / "rct-project/csp-gen").rglob("*.csp"))}
    return thy, dfy, rct, csp

def one(mid, study, src):
    out = OUT / mid; rec = out / "record.json"
    if rec.exists():
        return json.loads(rec.read_text())
    if out.exists(): shutil.rmtree(out)
    out.mkdir(parents=True)
    t0 = time.time(); r = {"id": mid, "study": study, "extractor": EXTRACTOR}
    with open(out / "driver.log", "w") as log:
        p = subprocess.run([str(JAVA), "-cp", f"src/main/resources:{CP}:{CLASSES}:build/resources/main", "CampaignDriver", str(src), str(out)],
                           cwd=F / "forge.transformations", stdout=log, stderr=subprocess.STDOUT, timeout=900)
    r["driver_rc"] = p.returncode
    r["driver_ok"] = "CAMPAIGN_DRIVER_OK" in (out / "driver.log").read_text(errors="ignore")
    rcts = sorted(out.glob("*.rct"))
    if rcts:
        proj = out / "rct-project"; proj.mkdir()
        for x in rcts: shutil.copy(x, proj)
        with open(out / "cspgen.log", "w") as log:
            q = subprocess.run([str(JAVA), "-cp", GCP, "circus.robocalc.robochart.generator.csp.Main", "true", "true", str(proj)],
                               cwd=proj, stdout=log, stderr=subprocess.STDOUT, timeout=900)
        r["cspgen_rc"] = q.returncode; r["csp_files"] = len(list((proj / "csp-gen").rglob("*.csp")))
    else:
        r["cspgen_rc"] = None; r["csp_files"] = 0
    # Dafny column: every .dfy the extractor produced (two for the Chemical Detector)
    dres = []
    for dfy in sorted(out.glob("*.dfy")):
        try:
            d = subprocess.run([DAFNY, "verify", str(dfy), "--solver-path", os.environ.get("Z3", "/opt/homebrew/bin/z3")], capture_output=True, text=True, timeout=120)
            txt = d.stdout + d.stderr; (out / f"dafny_{dfy.stem}.log").write_text(txt)
            m = re.search(r"finished with (\d+) verified, (\d+) errors?", txt)
            dres.append({"file": dfy.name, "rc": d.returncode, "verified": int(m.group(1)) if m else None, "errors": int(m.group(2)) if m else None,
                         "outcome": ("pass" if m and int(m.group(2)) == 0 else "fail" if m else "error")})
        except subprocess.TimeoutExpired:
            dres.append({"file": dfy.name, "rc": None, "verified": None, "errors": None, "outcome": "timeout"})
    r["dafny"] = dres
    r["elapsed_s"] = round(time.time() - t0, 1)
    rec.write_text(json.dumps(r, indent=1))
    return r

def main():
    ensure_driver_classes()
    subjects = [(f"baseline-{s}", s, F / "reference-runs" / s / "java") for s in ("lre", "sranger", "chemical_detector")]
    for d in sorted((M / "mutants").iterdir()):
        if (d / "java").is_dir():
            study = "chemical_detector" if d.name.startswith("chemical_detector") else d.name.split("_")[0]
            subjects.append((d.name, study, d / "java"))
    subjects = [s for s in subjects if s[2].exists()]
    print(f"[regen] extractor {EXTRACTOR}; subjects {len(subjects)}; parallel {PAR}", flush=True)
    # baselines first (serially) so comparisons can proceed
    for s in subjects[:3]:
        r = one(*s); print(f"[regen] {s[0]} driver_ok={r['driver_ok']} csp={r['csp_files']} dafny={[x['outcome'] for x in r['dafny']]} {r['elapsed_s']}s", flush=True)
    done = 0
    with ThreadPoolExecutor(PAR) as ex:
        futs = {ex.submit(one, *s): s for s in subjects[3:]}
        for fu in as_completed(futs):
            s = futs[fu]; done += 1
            try:
                r = fu.result()
                if done % 10 == 0 or not r["driver_ok"]:
                    print(f"[regen] {done}/{len(futs)} {s[0]} ok={r['driver_ok']} csp={r['csp_files']} dafny={[x['outcome'] for x in r['dafny']]}", flush=True)
            except Exception as e:
                print(f"[regen] {s[0]} EXC {e}", flush=True)
    # comparison + results.csv
    base = {s: artefacts(OUT / f"baseline-{s}") for s in ("lre", "sranger", "chemical_detector")}
    rows = []
    for mid, study, _ in subjects:
        rec = OUT / mid / "record.json"
        if not rec.exists(): continue
        r = json.loads(rec.read_text()); thy, dfy, rct, csp = artefacts(OUT / mid)
        b = base[study]
        def changed(a, bb): return set(a) != set(bb) or any(a[k] != bb[k] for k in a)
        rows.append({"mutant_id": mid, "study": study, "extractor": r["extractor"], "driver_ok": r["driver_ok"],
                     "model_changed_thy": "yes" if changed(thy, b[0]) else "no", "model_changed_dfy": "yes" if changed(dfy, b[1]) else "no",
                     "model_changed_rct": "yes" if changed(rct, b[2]) else "no", "csp_changed": "yes" if changed(csp, b[3]) else "no",
                     "csp_files": r["csp_files"],
                     "dafny_outcome": ";".join(f"{x['file']}={x['outcome']}" for x in r["dafny"]),
                     "dafny_killed": "yes" if any(x["outcome"] in ("fail",) for x in r["dafny"]) else ("timeout" if any(x["outcome"] == "timeout" for x in r["dafny"]) else "no"),
                     "dafny_errors": sum((x["errors"] or 0) for x in r["dafny"]), "elapsed_s": r["elapsed_s"]})
    with open(OUT / "results.csv", "w", newline="") as fh:
        w = csv.DictWriter(fh, fieldnames=list(rows[0].keys())); w.writeheader(); w.writerows(rows)
    print(f"[regen] results.csv: {len(rows)} rows", flush=True)

if __name__ == "__main__":
    main()
