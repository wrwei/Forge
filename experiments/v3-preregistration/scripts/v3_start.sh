#!/bin/bash
# v3_start.sh <cold|reference> <study> <k> -- prepare ONE pass or run of pre-registration v3
# (review/prereg-v3-study-free.md) and open it for the operator. Refuses to start
# number k of a study while k-1 of the same collection is not committed, and (Amendment 3)
# any session but the next one in the pre-registered order.
set -u
COND="${1:?cold|reference}"; STUDY="${2:?study}"; K="${3:?number}"
C=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
PR=/Users/ranwei/Git/TOSEM_Formal_Guided_Vibe_Coding/review/v3-prompts
case "$COND" in
  cold)      COLL=v3-cold;      PFX=forge-v3c9; STG=/Users/ranwei/Gitee/v3c9-staging ;;
  reference) COLL=v3-reference; PFX=forge-v3r10; STG=/Users/ranwei/Gitee/v3r10-staging ;;
  *) echo "FAIL condition must be cold or reference"; exit 1 ;;
esac
# Amendments 15 and 16: the Steam Boiler is re-collected at the Amendment 16 pin, with new names.
if [ "$STUDY" = steam_boiler ]; then
  case "$COND" in cold) PFX=forge-v3c16; STG=/Users/ranwei/Gitee/v3c16-staging ;; reference) PFX=forge-v3r16; STG=/Users/ranwei/Gitee/v3r16-staging ;; esac
fi
PROMPT="$PR/v3-$COND-$STUDY-run$K.md"
[ -f "$PROMPT" ] || { echo "FAIL no pre-registered prompt $PROMPT"; exit 1; }
export FORGE_ARM_DIR=$C/experiments/$COLL FORGE_WT_PREFIX=$PFX FORGE_STAGE_DIR=$STG \
       FORGE_SHA=56cb43d7835b43e8a986e6ba4c0e82f35083d6f1
[ "$STUDY" = steam_boiler ] && export FORGE_SHA=aaddf2225b38eb1eb749e816f6f5874d7ec93eaa   # Amendments 14-16
if [ "$K" -gt 1 ]; then
  PREV=$((K-1))
  [ -d "${V3_ORDER_ROOT:-$C/experiments}/$COLL/$STUDY/run-$PREV/iter-1" ] || { echo "FAIL $COND $STUDY $PREV is not committed yet; finish it first (v3_finish.sh)"; exit 1; }
fi
# Amendment 3: only the NEXT session of the pre-registered order may start. Order:
# cold SRanger k, cold LRE k, cold Chemical Detector k (k = 1..10); Reference round j
# (SRanger, LRE, Chemical Detector) after cold round 2j (j = 1..5); the Steam Boiler
# Reference run after Reference round 5. "Next" is the first session in that order
# whose iter-1 is not committed (V3_ORDER_ROOT overrides the collections' root, for the stub test).
OR=${V3_ORDER_ROOT:-$C/experiments}
NEXT=""
for k in 1 2 3 4 5 6 7 8 9 10; do
  for s in sranger lre chemical_detector; do
    [ -z "$NEXT" ] && [ ! -d "$OR/v3-cold/$s/run-$k/iter-1" ] && NEXT="cold $s $k"
  done
  if [ $((k % 2)) = 0 ]; then j=$((k / 2))
    for s in sranger lre chemical_detector; do
      [ -z "$NEXT" ] && [ ! -d "$OR/v3-reference/$s/run-$j/iter-1" ] && NEXT="reference $s $j"
    done
  fi
done
# Amendment 15 (replacing the order of Amendments 11 and 12): the Steam Boiler, re-collected
# at the Amendment 14 pin: cold k (k = 1..10), with Reference run j after cold pass 2j
# (j = 1..5); void replacements (cold 11, 12, ...; Reference 6, 7, ...) after.
for k in 1 2 3 4 5 6 7 8 9 10; do
  [ -z "$NEXT" ] && [ ! -d "$OR/v3-cold/steam_boiler/run-$k/iter-1" ] && NEXT="cold steam_boiler $k"
  if [ $((k % 2)) = 0 ]; then j=$((k / 2))
    [ -z "$NEXT" ] && [ ! -d "$OR/v3-reference/steam_boiler/run-$j/iter-1" ] && NEXT="reference steam_boiler $j"
  fi
done
for coll in cold reference; do
  [ $coll = cold ] && base=11 || base=6
  nv=$(ls "$OR/v3-$coll/steam_boiler"/run-*/VOID.md 2>/dev/null | wc -l | tr -d ' '); i=0
  while [ "$i" -lt "$nv" ]; do k=$((base + i)); [ -z "$NEXT" ] && [ ! -d "$OR/v3-$coll/steam_boiler/run-$k/iter-1" ] && NEXT="$coll steam_boiler $k"; i=$((i + 1)); done
done
# Replacements (Validity section): each committed session with a VOID.md is replaced by
# the next number of its study and collection, after the order above; cold before
# Reference, studies in the order above.
for coll in cold reference; do
  [ $coll = cold ] && base=11 || base=6
  for s in sranger lre chemical_detector; do
    nv=$(ls "$OR/v3-$coll/$s"/run-*/VOID.md 2>/dev/null | wc -l | tr -d ' ')
    i=0; while [ "$i" -lt "$nv" ]; do
      k=$((base + i)); [ -z "$NEXT" ] && [ ! -d "$OR/v3-$coll/$s/run-$k/iter-1" ] && NEXT="$coll $s $k"; i=$((i + 1))
    done
  done
done
[ -n "$NEXT" ] || { echo "FAIL every pre-registered session is already committed"; exit 1; }
[ "$COND $STUDY $K" = "$NEXT" ] || { echo "FAIL the next session in the pre-registered order is: $NEXT"; exit 1; }
[ -n "${V3_ORDER_ROOT:-}" ] && { echo "ORDER OK $NEXT"; exit 0; }
cd "$C" || exit 1
# Amendment 9: the recorder must accept the study before any session starts.
REC=/Users/ranwei/Git/TOSEM_Formal_Guided_Vibe_Coding/review/codex-prompts/snapshot_v2.py
python3 "$REC" -h 2>/dev/null | grep -q "[{,]$STUDY[,}]" || { echo "FAIL the recorder does not accept study $STUDY -- do not start the session"; exit 1; }
echo "OK   the recorder accepts $STUDY (Amendment 9)"
bash scripts/setup_claude_run.sh "$STUDY" "$K" || { echo "FAIL setup -- do not start the session"; exit 1; }
pbcopy < "$PROMPT"
echo
echo "Prompt for $COND $STUDY $K is on the clipboard (paste its TEXT; do not attach the file)."
echo "In a NEW Claude Code session in $PFX-run$K-$STUDY: type /model, confirm Claude Opus 5.5, then paste."
echo "Close every editor tab in that VS Code window before pasting (the IDE tells the session which file is open)."
echo "When the session stops: /export transcript.md"
code --new-window "/Users/ranwei/Gitee/$PFX-run$K-$STUDY" 2>/dev/null || true
