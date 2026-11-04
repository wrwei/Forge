#!/bin/bash
# v3_strip_study_names.sh <worktree> [--check]
# Pre-registration v3, Amendment 8. Removes from the worktree every line of
# .gitignore, and every comment line of pipeline.yaml, that names a study
# (joined and prefixed forms included). With --check, removes nothing and exits 1
# if any such line is present.
set -u
WT="${1:?worktree}"; MODE="${2:-}"
PAT='S-?Ranger|SRANGER|sranger|\bLRE|\blre\b|\bLre[A-Z_]|\bLre\b|[Cc]hemical|CHEMICAL|[Cc]hem[-_]?[Dd]etector|chemdetector|CHEMDETECTOR|[Gg]as ?[Aa]nalysis|GAS ?ANALYSIS|[Ss]team|STEAM|[Bb]oiler|BOILER'
n_gi=0; n_py=0
[ -f "$WT/.gitignore" ] && n_gi=$(perl -ne "print if /$PAT/" "$WT/.gitignore" | wc -l | tr -d ' ')
[ -f "$WT/pipeline.yaml" ] && n_py=$(perl -ne "print if /^\s*#/ && /$PAT/" "$WT/pipeline.yaml" | wc -l | tr -d ' ')
if [ "$MODE" = "--check" ]; then
  [ "$n_gi" = 0 ] && [ "$n_py" = 0 ] && exit 0
  echo "   .gitignore: $n_gi line(s), pipeline.yaml: $n_py comment line(s) name a study"; exit 1
fi
[ "$n_gi" != 0 ] && perl -i -ne "print unless /$PAT/" "$WT/.gitignore"
[ "$n_py" != 0 ] && perl -i -ne "print unless /^\s*#/ && /$PAT/" "$WT/pipeline.yaml"
echo "   removed $n_gi line(s) from .gitignore and $n_py comment line(s) from pipeline.yaml that name a study (pre-registration v3, Amendment 8)"
