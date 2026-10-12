#!/usr/bin/env bash
# Regenerate the Isabelle theories for the listed mutants with the current
# extractor (same three phases as scripts/generate_isabelle.sh), reading each
# mutant's Java from experiments/mutation-testing/mutants/<id>/java.
#   bash generate_mutant_theories.sh <out-dir> <id-list-file>
# Requires JDK 21 and a built extractor: (cd forge.transformations && ./gradlew classes)
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$HERE/../../.." && pwd)"
T2M="$REPO_ROOT/forge.transformations"
OUT="${1:?usage: generate_mutant_theories.sh <out-dir> <id-list-file>}"
LIST="${2:?usage: generate_mutant_theories.sh <out-dir> <id-list-file>}"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
CLASSES="${FORGE_CLASSES:-$T2M/build/classes/java/main}"
[ -d "$CLASSES" ] || { echo "No compiled extractor at $CLASSES"; exit 2; }
if [ -n "${FORGE_CP:-}" ]; then CP="$FORGE_CP"; else
  CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/caches/modules-2/files-2.1"
  CP=$(find "$CACHE" -name "*.jar" 2>/dev/null | grep -v -e -sources -e -javadoc | grep -v "epsilon.*2\.5\.0" \
        | grep -vE "org\.eclipse\.jdt/(org\.eclipse\.jdt\.core|ecj)/3\.(33|37)\.0" | tr "\n" ":")
fi
mkdir -p "$OUT"; OUT="$(cd "$OUT" && pwd)"
n=0; ok=0
while read -r mid; do
  [ -n "$mid" ] || continue
  n=$((n+1)); o="$OUT/$mid"; rm -rf "$o"; mkdir -p "$o"
  src="$REPO_ROOT/experiments/mutation-testing/mutants/$mid/java"
  good=1
  for ph in t2m m2m isabelle_gen; do
    ( cd "$T2M" && "$JAVA" -cp "src/main/resources:${CP}${CLASSES}" forge.transformations.core.App \
        $ph source="$src" output="$o" ) >> "$o/gen.log" 2>&1 || { good=0; break; }
  done
  thy=$(find "$o" -name "*_Beh.thy" | wc -l | tr -d ' ')
  [ "$good" = "1" ] && [ "$thy" -ge 1 ] && ok=$((ok+1)) || echo "FAIL $mid (thy=$thy)"
done < "$LIST"
echo "DONE: $ok/$n generated"
