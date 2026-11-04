#!/bin/bash
# test_instruction_leak_gate.sh <clean-tree> -- seeded-leak tests for instruction_leak_gate.py.
# <clean-tree> is a worktree (or copy) whose instructions pass the gate. Each case copies it,
# seeds one change, and checks the gate's exit status. Prints one line per case; exit 1 if any fails.
set -u
CLEAN="${1:?clean tree}"; GATE="$(cd "$(dirname "$0")" && pwd)/instruction_leak_gate.py"; PY="${PYTHON:-python3}"; bad=0
T=$(mktemp -d "${TMPDIR:-/tmp}/leakgate.XXXXXX") || { echo "FAIL cannot create a temporary directory"; exit 2; }
[ -d "$T" ] || { echo "FAIL no temporary directory"; exit 2; }
t() { local name=$1 want=$2; shift 2; rm -rf "$T/w"; cp -R "$CLEAN" "$T/w"; ( cd "$T/w" && eval "$@" ); "$PY" "$GATE" "$T/w" --feedback ${GATE_ARGS:-} >/dev/null 2>&1; local got=$?
      [ "$got" = "$want" ] && r=ok || { r=FAIL; bad=1; }; printf "  %-55s want=%s got=%s %s\n" "$name" "$want" "$got" "$r"; }
L=forge.transformations/src/main/java/forge/transformations/preflight/StructuralLinter.java
E=forge.transformations/src/main/resources/transformations/thy_generation_rule.egl
t "clean tree"                                0 "true"
t "tree without instruction files"           2 "rm -f CLAUDE.md"
t "study name in a rule file"                 1 "echo 'As in the SRanger study.' >> forge.assets/prompts/java_codegen_rules.txt"
t "requirement-ID prefix in a layer prompt"   1 "echo 'See LRE-OP5.' >> forge.assets/vibe-coding-prompts/03_leaves.md"
t "reference-run identifier (camelCase)"      1 "echo 'e.g. closestStaticIndex' >> CLAUDE.md"
t "reference-run identifier (snake_case)"     1 "echo 'e.g. HCM_to_CAM' >> CLAUDE.md"
t "requirements acronym"                      1 "echo 'e.g. OPEZ' >> CLAUDE.md"
t "domain word"                               1 "echo 'e.g. a water level' >> CLAUDE.md"
t "new prompt file carrying a leak"           1 "echo 'gas sensor' > forge.assets/prompts/extra.txt"
t "leak in a Python feedback string"          1 "printf '\nMSG = \"see the autopilot\"\n' >> forge.dashboard/web/vacuity.py"
t "leak in a Java fix directive"              1 "printf '\nclass X { String s = \"as in LreController\"; }\n' >> $L"
t "leak in emitted theory text"               1 "printf '\n(* measured on the boiler *)\n' >> $E"
t "leak only in a Python comment"             0 "printf '\n# measured on the LRE study\n' >> forge.dashboard/web/vacuity.py"
t "leak only in a Python docstring"           0 "printf '\ndef f():\n    \"\"\"Measured on the LRE study.\"\"\"\n    return 1\n' >> forge.dashboard/web/vacuity.py"
t "leak only in a Java comment"               0 "printf '\n// measured on the LRE study\n' >> $L"
t "leak only in a template code comment"      0 "printf '\n[%% // measured on the LRE study %%]\n' >> $E"
t "developer notes outside the read set"      0 "mkdir -p docs && echo 'LRE, SRanger, chemical detector' > docs/DEVELOPING.md"
t "study name in the corpus README"           1 "mkdir -p docs/corpus && echo 'see the LRE theories' >> docs/corpus/README.md"
t "study name in the Gradle scaffold"          1 "echo \"rootProject.name = 'chem-detector'\" > java.generated.project/settings.gradle"
t "joined study name in the scaffold"          1 "sed -i.bak \"s/^group = .*/group = 'chemdetector'/\" java.generated.project/build.gradle && rm java.generated.project/build.gradle.bak"
t "study name in pipeline.yaml"                1 "printf '\\n  note: as for the steam boiler\\n' >> pipeline.yaml"
t "study name in a pipeline.yaml comment"      1 "printf '\\n# as for the steam boiler\\n' >> pipeline.yaml"
t "prefixed study name in a pipeline.yaml comment" 1 "printf '\\n    # LreController_Check discharges in ~71 s.\\n' >> pipeline.yaml"
t "study name in .gitignore"                   1 "echo 'sranger-requirements-junit5/' >> .gitignore"
t "underscored study name in .gitignore"       1 "echo 'LRE_REQUIREMENTS_REFERENCE_SPEC.md' >> .gitignore"
t "ordinary word containing lre"               0 "echo 'as already noted' >> CLAUDE.md"
t "the session's own active_case_study line"   0 "sed -i.bak 's/^  active_case_study: .*/  active_case_study: lre/' pipeline.yaml && rm pipeline.yaml.bak"
t "LRE-named file in forge.assets/corrections" 1 "printf -- '-- CSP-M instantiation overrides for the LRE controller\\n' > forge.assets/corrections/csp_overrides.csp"
t "study-free file in forge.assets/corrections" 0 "printf -- '-- generate nat\\nnametype core_nat = {0..1}\\n' > forge.assets/corrections/csp_overrides.csp"
GATE_ARGS=--no-archive t "docs/archive present (--no-archive)"  1 "mkdir -p docs/archive && touch docs/archive/x.thy"
GATE_ARGS=--no-archive t "docs/archive absent (--no-archive)"   0 "rm -rf docs/archive"
rm -rf "$T"; exit $bad
