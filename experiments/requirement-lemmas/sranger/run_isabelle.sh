#!/usr/bin/env bash
# Build the reference theory with the requirement-derived lemmas, and the control (wrong target
# mode in MovingToTurning), each under its own session name so the full error log can be read
# back. Prints one line per build and the requirement lemmas that failed.
cd "$(dirname "$0")"
ISA=${ISA:-"$(cd ../../.. && pwd)/scripts/isabelle-docker.sh"}
TO=${TO:-1800}   # whole-build bound; a failing lemma must not stop the others being checked
mkdir -p results
for id in reqlemmas control; do
  dir=$id/isabelle; sess="reqlem_sranger_$id"
  sed -i '' -E "s/^session +[A-Za-z0-9_]+/session $sess/" "$dir/ROOT"
  t0=$(date +%s)
  "$ISA" build -D "$dir" -v -o timeout=$TO > "results/$id.isabelle.log" 2>&1; rc=$?
  t=$(( $(date +%s) - t0 ))
  "$ISA" build_log -H Error "$sess" > "results/$id.isabelle.errors" 2>/dev/null
  thy="$dir/SRangerController_Beh.thy"
  failed=$(grep -oE '\(line [0-9]+ of' "results/$id.isabelle.errors" | grep -oE '[0-9]+' | sort -n | uniq | while read l; do
      awk -v l=$l 'NR<=l && /^lemma /{n=$2} NR==l{print n}' "$thy"; done | tr -d ':' | sort -u | tr '\n' ' ')
  nreq=$(grep -c '^lemma Req_' "$thy")
  if [ $rc -ne 0 ] && [ -z "$failed" ]; then
    echo "$id: NO VERDICT (exit=$rc ${t}s): $(grep -m1 -iE 'docker|error|\*\*\*' results/$id.isabelle.log | cut -c1-160)"
  else
    echo "$id: exit=$rc ${t}s; $nreq requirement lemmas; failed: ${failed:-none}"
  fi
done | tee results/summary.txt
echo "=== DONE"
