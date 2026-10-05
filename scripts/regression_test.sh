#!/usr/bin/env bash
# Pipeline regression test: runs M2M -> RctPhase -> CSP generation on two
# pinned Java sources and checks the extracted state-machine structure and the
# generated CSP module files. Phases 3-5 contain no LLM step, so the same
# source must give the same structure on every run.
#
#   chemical_detector  java.generated.project/src/main/java   expect 13 states
#   lre                scripts/regression-fixtures/lre/        expect 4 states
#
# The LRE fixture is the LRE controller source the paper's regression check
# pins (development commit 8ffd187), bundled here so the check needs nothing
# outside this repository.
#
# Usage (from repo root):   bash scripts/regression_test.sh
# Requires a JDK 21 (JAVA_HOME) and a built extractor:
#   (cd forge.transformations && ./gradlew classes)
# Exit code = number of failing case studies (0 = all pass).
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T2M="$REPO_ROOT/forge.transformations"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
CLASSES="${FORGE_CLASSES:-$T2M/build/classes/java/main}"
WORK="$(mktemp -d "${TMPDIR:-/tmp}/forge-regression.XXXXXX")"
trap 'rm -rf "$WORK"' EXIT
PASS=0
FAIL=0

if [ ! -d "$CLASSES" ]; then
  echo "No compiled extractor at $CLASSES."
  echo "Build it first:  (cd forge.transformations && ./gradlew classes)"
  exit 2
fi

# Runtime classpath: FORGE_CP if given, else the Gradle dependency cache.
if [ -n "${FORGE_CP:-}" ]; then
  CP="$FORGE_CP"
else
  CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/caches/modules-2/files-2.1"
  # One copy of each library: the newest JDT core/ecj, and no Epsilon 2.5.0
  # (a duplicate Epsilon on the classpath stops the ETL resolving its classes).
  CP=$(find "$CACHE" -name "*.jar" 2>/dev/null | grep -v -e -sources -e -javadoc \
        | grep -v "epsilon.*2\.5\.0" \
        | grep -vE "org\.eclipse\.jdt/(org\.eclipse\.jdt\.core|ecj)/3\.(33|37)\.0" | tr "\n" ":")
fi
CSPGEN_CP=$(find "$T2M/lib/robochart-csp-gen" -name "*.jar" | tr "\n" ":")

run_case() {
  local name="$1" source_dir="$2" expect_states="$3"
  local out="$WORK/$name"
  echo
  echo "=== $name ==="
  mkdir -p "$out"
  ( cd "$T2M" && "$JAVA" -cp "src/main/resources:${CP}${CLASSES}" \
      forge.transformations.core.App m2m source="$source_dir" output="$out" ) > "$out/m2m.log" 2>&1
  local actual_states
  actual_states=$(grep -m1 "RoboChart:" "$out/m2m.log" | sed 's/.*RoboChart: \([0-9]*\) states.*/\1/')
  if [ -z "$actual_states" ]; then
    echo "FAIL: M2M produced no model for $name"; tail -10 "$out/m2m.log"
    FAIL=$((FAIL + 1)); return
  fi
  if [ "$actual_states" != "$expect_states" ]; then
    echo "FAIL: $name produced $actual_states states (expected $expect_states)"
    FAIL=$((FAIL + 1)); return
  fi
  if ! ( cd "$T2M" && "$JAVA" -cp "src/main/resources:${CP}${CLASSES}" \
      forge.transformations.core.App forge.transformations.m2t.RctPhase output="$out" ) > "$out/rct.log" 2>&1; then
    echo "FAIL: RctPhase failed for $name"; tail -5 "$out/rct.log"
    FAIL=$((FAIL + 1)); return
  fi
  ( cd "$out" && "$JAVA" -cp "$CSPGEN_CP" circus.robocalc.robochart.generator.csp.Main true true "$out" ) > "$out/csp.log" 2>&1
  if grep -q "^ERROR:" "$out/csp.log"; then
    echo "FAIL: CSP generation reported errors for $name"; grep "^ERROR:" "$out/csp.log" | head -3
    FAIL=$((FAIL + 1)); return
  fi
  local module_count
  module_count=$(ls "$out/csp-gen/defs/" 2>/dev/null | grep -c "_Module\.csp$" || true)
  if [ "$module_count" -lt 1 ]; then
    echo "FAIL: $name produced no module CSP files"
    FAIL=$((FAIL + 1)); return
  fi
  echo "PASS: $name ($actual_states states, $module_count module CSP file(s))"
  PASS=$((PASS + 1))
}

run_case "chemical_detector" "$REPO_ROOT/java.generated.project/src/main/java" 13
run_case "lre" "$REPO_ROOT/scripts/regression-fixtures/lre" 4

echo
echo "=== Summary ==="
echo "Passed: $PASS"
echo "Failed: $FAIL"
exit "$FAIL"
