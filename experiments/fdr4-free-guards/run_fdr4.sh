#!/usr/bin/env bash
# Run FDR4 on every staged model (prepare.py), one model at a time.
# Output: results/<model>.json (framed JSON from refines) and results.csv
set -u
FDR=${FDR:-/Applications/FDR4.app/Contents/MacOS/refines}
HERE="$(cd "$(dirname "$0")" && pwd)"
ST="$HERE/${1:-staged}"; OUT="$HERE/${2:-results}"; mkdir -p "$OUT"
[ -x "$FDR" ] || { echo "no refines binary at $FDR"; exit 2; }
printf 'channel a\nP = a -> STOP\nassert P [T= P\n' > "$OUT/preflight.csp"
perl -e 'alarm 120; exec @ARGV' "$FDR" --format framed_json "$OUT/preflight.csp" > "$OUT/preflight.json" 2>&1
grep -qE '"result": *[0-9]' "$OUT/preflight.json" || { echo "pre-flight: no verdict (licence?)"; head -5 "$OUT/preflight.json"; exit 3; }
echo "model,exit,seconds,deadlock_free,deadlock_free_term,divergence_free,enter_OCM,enter_MOM,enter_HCM,enter_CAM" > "$OUT/results.csv"
for d in "$ST"/*/; do
  m=$(basename "$d"); t0=$(date +%s)
  ( cd "$d/defs" && perl -e 'alarm 1800; exec @ARGV' "$FDR" --format framed_json free_guards_assertions.csp ) > "$OUT/$m.json" 2> "$OUT/$m.err"
  rc=$?; t1=$(date +%s)
  row=$(/usr/bin/python3 - "$OUT/$m.json" <<'PY'
import json,sys,re
raw=open(sys.argv[1]).read()
res=[(r["assertion_string"],r["result"]) for d in re.findall(r"\{.*\}",raw) for r in (json.loads(d).get("results") or [])] if raw.strip() else []
def v(pat):
    hit=[r for a,r in res if re.search(pat,a)]
    return ("PASS" if hit[0]==1 else "FAIL") if hit else "none"
print(",".join([v(r"^P_LreController :\[deadlock"),v(r"RUN\(\{r__\}\)"),v(r"divergence"),
                v(r"OCM::entered"),v(r"MOM::entered"),v(r"HCM::entered"),v(r"CAM::entered")]))
PY
)
  echo "$m,$rc,$((t1-t0)),$row" | tee -a "$OUT/results.csv"
done
echo "=== DONE ==="
