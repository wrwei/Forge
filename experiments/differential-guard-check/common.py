"""Shared driver: run a Java harness over scenario lines, evaluate the theory, compare."""
import os, re, subprocess
from fractions import Fraction
from model_eval import Real, Env, parse_theory, refreshed_vars, admitted, Undecided

JAVA = "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin"


def dbl(s):
    s = s.strip()
    if s in ("NaN",): return float("nan")
    if s in ("Infinity", "-Infinity"): return float(s.replace("Infinity", "inf"))
    return float.fromhex(s) if "0x" in s else float(s)


def compile_harness(src_root, harness, out, exclude=("/app/",)):
    os.makedirs(out, exist_ok=True)
    files = [os.path.join(d, f) for d, _, fs in os.walk(src_root) for f in fs if f.endswith(".java")]
    files = [f for f in files if not any(x in f for x in exclude)]
    r = subprocess.run([f"{JAVA}/javac", "-d", out] + files + [harness], capture_output=True, text=True)
    if r.returncode: raise RuntimeError(r.stderr[-3000:])


def run_harness(cp, main, lines):
    r = subprocess.run([f"{JAVA}/java", "-cp", cp, main], input="\n".join(lines) + "\n", capture_output=True, text=True)
    if r.returncode: raise RuntimeError(r.stderr[-3000:])
    out = {}
    for l in r.stdout.splitlines():
        t = l.split()
        rec = {"next": t[1], "fields": {}, "funcs": {}}
        for kv in t[2:]:
            k, v = kv.split("=", 1)
            if k.startswith("F:"):
                parts = k[2:].split(":")
                if len(parts) == 1: rec["funcs"][parts[0]] = v
                else: rec["funcs"].setdefault(parts[0], {})[int(parts[1])] = v
            else: rec["fields"][k] = v
        out[t[0]] = rec
    return out
