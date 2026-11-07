import json, os, subprocess, glob, re, shutil, sys, concurrent.futures as cf
W = os.path.abspath("."); JAVA = "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin/java"
CPX = open(f"{W}/cp.txt").read().strip()
sys.path.insert(0, W); from canon import canon
V = ("a16", "a19")
PH = [("forge.transformations.t2m.T2mPhase", True), ("forge.transformations.m2m.TransformPhase", True),
      ("forge.transformations.m2t.DafnyPhase", True), ("forge.transformations.m2t.RctPhase", False), ("forge.transformations.m2t.IsabellePhase", False)]
def extract(src, out, variant):
    os.makedirs(out, exist_ok=True)
    for cls, needs_src in PH:
        a = [JAVA, "-cp", f"{W}/classes-{variant}:{CPX}", "forge.transformations.core.App", cls] + ([f"source={src}"] if needs_src else []) + [f"output={out}"]
        r = subprocess.run(a, capture_output=True, text=True, timeout=900)
        open(f"{out}/drv.log", "a").write(r.stdout[-4000:] + r.stderr[-4000:])
        if r.returncode != 0: return False, cls
    return True, None
def arts(out):
    d = {}
    for f in glob.glob(out + "/*.rct") + glob.glob(out + "/*.dfy"):
        d[os.path.basename(f)] = sorted(l for l in open(f).read().splitlines() if l.strip() and not l.strip().startswith("//"))
    for f in glob.glob(out + "/isabelle/*_Beh.thy"):
        d["isabelle/" + os.path.basename(f)] = canon(open(f).read())
    return d
def one(i, j, keep):
    base = f"{W}/ra19/{i:03d}"; shutil.rmtree(base, ignore_errors=True)
    src = f"{base}/src"
    if j["kind"] == "root": shutil.copytree(j["arg"], src)
    else: os.makedirs(src); shutil.copytree(j["dir"], f"{src}/{j['arg']}")
    res = {v: extract(src, f"{base}/out-{v}", v) for v in V}
    rec = dict(i=i, group=j["group"], dir=j["dir"], orig_ok=res[V[0]][0], fix_ok=res[V[1]][0], fail_where=[res[V[0]][1], res[V[1]][1]])
    if res[V[0]][0] and res[V[1]][0]:
        a, b = arts(f"{base}/out-{V[0]}"), arts(f"{base}/out-{V[1]}")
        diffs = {}
        for k in sorted(set(a) | set(b)):
            x, y = a.get(k, []), b.get(k, [])
            add = [l for l in y if l not in x]; rem = [l for l in x if l not in y]
            if add or rem: diffs[k] = dict(added=add, removed=rem)
        rec["diffs"] = diffs
    # Amendment 16: does the fixed model send an integer literal outside {0,1} on an int/nat event?
    rct = glob.glob(f"{base}/out-{V[1]}/*.rct")
    if rct:
        t = open(rct[0]).read()
        evt = dict(re.findall(r"^\s*event\s+(\w+)\s*:\s*(int|nat)\s*$", t, re.M))
        hits = sorted({f"{e}!{v}" for l in t.splitlines() if re.match(r"\s*(entry|exit|during|action)\b", l)
                       for e, v in re.findall(r"\b(\w+)\s*!\s*(-?\d+)\b(?!\s*\.)", l) if e in evt and not 0 <= int(v) <= 1})
        rec["literal_sends_outside_01"] = hits
    shutil.rmtree(src, ignore_errors=True)
    if not keep and not rec.get("diffs") and rec["orig_ok"] and rec["fix_ok"]: shutil.rmtree(base, ignore_errors=True)
    return rec
if __name__ == "__main__":
    jobs = json.load(open(sys.argv[1])); outp = sys.argv[2]; keep = len(sys.argv) > 3
    out = []
    with cf.ThreadPoolExecutor(8) as ex:
        futs = [ex.submit(one, i, j, keep) for i, j in enumerate(jobs)]
        for n, f in enumerate(cf.as_completed(futs), 1):
            out.append(f.result())
            if n % 25 == 0: print(f"[reg] {n}/{len(jobs)}", flush=True)
    out.sort(key=lambda r: r["i"]); json.dump(out, open(outp, "w"), indent=1)
    print("[reg] done", len(out))
