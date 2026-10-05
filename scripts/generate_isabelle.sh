#!/usr/bin/env bash
# Regenerate the Isabelle theories of the three case studies with the current
# extractor, from the Java in reference-runs/ (one directory per study).
#   bash scripts/generate_isabelle.sh <out-dir>
# Requires JDK 21 and a built extractor: (cd forge.transformations && ./gradlew classes)
set -uo pipefail
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T2M="$REPO_ROOT/forge.transformations"
OUT="${1:?usage: generate_isabelle.sh <out-dir>}"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
CLASSES="${FORGE_CLASSES:-$T2M/build/classes/java/main}"
[ -d "$CLASSES" ] || { echo "No compiled extractor at $CLASSES; run (cd forge.transformations && ./gradlew classes)"; exit 2; }
if [ -n "${FORGE_CP:-}" ]; then CP="$FORGE_CP"; else
  CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/caches/modules-2/files-2.1"
  CP=$(find "$CACHE" -name "*.jar" 2>/dev/null | grep -v -e -sources -e -javadoc | grep -v "epsilon.*2\.5\.0" \
        | grep -vE "org\.eclipse\.jdt/(org\.eclipse\.jdt\.core|ecj)/3\.(33|37)\.0" | tr "\n" ":")
fi
mkdir -p "$OUT"; OUT="$(cd "$OUT" && pwd)"
fail=0
for st in lre sranger chemical_detector; do
  o="$OUT/$st"; rm -rf "$o"; mkdir -p "$o"
  for ph in t2m m2m isabelle_gen; do
    ( cd "$T2M" && "$JAVA" -cp "src/main/resources:${CP}${CLASSES}" forge.transformations.core.App \
        $ph source="$REPO_ROOT/reference-runs/$st/java" output="$o" ) >> "$o/gen.log" 2>&1 \
      || { echo "FAIL: $st $ph (see $o/gen.log)"; fail=1; break; }
  done
  n=$(find "$o" -name "*_Beh.thy" | wc -l | tr -d " ")
  echo "$st: $n theory file(s)"
done
exit $fail
