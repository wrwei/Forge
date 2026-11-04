#!/bin/bash
# v3_finish.sh <cold|reference> <study> <k> "<one-line verdict>" -- promote ONE audited pass or
# run of pre-registration v3 into its collection and commit it (never pushes). Run only
# after TRANSCRIPT_AUDIT.md is in the run directory.
set -u
COND="${1:?cold|reference}"; STUDY="${2:?study}"; K="${3:?number}"; MSG="${4:?verdict}"
C=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
case "$COND" in
  cold)      COLL=v3-cold;      PFX=forge-v3c9; STG=/Users/ranwei/Gitee/v3c9-staging; LABEL="Cold v3" ;;
  reference) COLL=v3-reference; PFX=forge-v3r10; STG=/Users/ranwei/Gitee/v3r10-staging; LABEL="Reference v3" ;;
  *) echo "FAIL condition must be cold or reference"; exit 1 ;;
esac
# Amendments 15 and 16: the Steam Boiler is re-collected at the Amendment 16 pin, with new names.
if [ "$STUDY" = steam_boiler ]; then
  case "$COND" in cold) PFX=forge-v3c16; STG=/Users/ranwei/Gitee/v3c16-staging ;; reference) PFX=forge-v3r16; STG=/Users/ranwei/Gitee/v3r16-staging ;; esac
fi
R=$C/experiments/$COLL/$STUDY/run-$K; S=$STG/$STUDY/run-$K
ls -d "$S"/iter-* >/dev/null 2>&1 || { echo "FAIL no staged iteration under $S"; exit 1; }
if [ "$COND" = cold ]; then
  # numbered iterations only; a kept setup-fault record (Amendment 4) sits beside iter-1
  [ "$(ls -d "$S"/iter-* | grep -cE '/iter-[0-9]+$')" = "1" ] && [ -d "$S/iter-1" ] || { echo "FAIL a cold pass must have exactly iter-1 staged"; exit 1; }
  for _x in "$S"/iter-*; do case "$(basename "$_x")" in iter-[0-9]*-setup-fault|iter-1) ;; *) echo "FAIL unexpected staged folder $(basename "$_x")"; exit 1 ;; esac; done
fi
WTT="/Users/ranwei/Gitee/$PFX-run$K-$STUDY/transcript.md"
if [ ! -f "$R/transcript.md" ] && [ -f "$WTT" ]; then mv "$WTT" "$R/transcript.md" && echo "moved $WTT into the run directory"; fi
[ -f "$R/transcript.md" ] || { echo "FAIL no transcript at $R/transcript.md"; exit 1; }
[ -f "$R/TRANSCRIPT_AUDIT.md" ] || { echo "FAIL no audit at $R/TRANSCRIPT_AUDIT.md -- ask for the audit first"; exit 1; }
for d in "$S"/iter-*; do [ ! -e "$R/$(basename "$d")" ] || { echo "FAIL $R/$(basename "$d") already exists"; exit 1; }; done
# (Amendment 13's split applied only to the superseded Reference run 2; Amendment 15
# re-collects the Steam Boiler with no cap, so every iteration is copied as usual.)
if false; then
  # Amendment 13: iterations 1-7 are the run's pre-registered result at the cap; iterations
  # 8 onward are its continuation past the cap, kept and reported separately.
  for d in "$S"/iter-*; do
    n=$(basename "$d" | sed 's/^iter-//'); case "$n" in ''|*[!0-9]*) echo "FAIL unexpected staged folder $(basename "$d")"; exit 1 ;; esac
    if [ "$n" -le 7 ]; then cp -R "$d" "$R/" || exit 1
    else mkdir -p "$R/continuation-a13" && cp -R "$d" "$R/continuation-a13/" || exit 1; fi
  done
else
  cp -R "$S"/iter-* "$R/" || exit 1
fi
cd "$C" && git add "experiments/$COLL/$STUDY/run-$K" \
  && git commit -q -m "$LABEL, $STUDY $K: $MSG" \
  && git log -1 --format='committed %h %s'
