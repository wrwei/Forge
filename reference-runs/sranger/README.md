# Case Study: SRanger

A small autonomous ground robot adapted from the published University of York
[RoboStar case-study set](https://robostar.cs.york.ac.uk/case_studies/sranger/index.html).
A single controller drives the vehicle forward, rotates in place when an
obstacle is detected within a threshold distance, and terminates on an
end-task command, across **three** operating modes (Moving, Turning, Final)
and 7 transitions.

## Summary

| Metric                              | Value |
| ----------------------------------- | ----- |
| Controllers                         | 1     |
| Operating modes                     | 3     |
| Transitions                         | 7     |
| Requirements                        | 23    |
| Java files                          | 11    |
| Java LOC                            | 600   |
| Snapshot from iteration             | 5 (one trajectory; see note) |
| Dafny verification                  | 5 verified, 0 errors |
| FDR4 (CSP)                          | deadlock-free, divergence-free pass; `[deterministic]` fails on `endTask.in` (expected nondeterminism from overlapping else-if guards) |
| Isabelle/UTP build wall-clock       | ~23 s |

> **Snapshot note.** This artefact is from one converged trajectory; the
> iteration index above is that trajectory's converging iteration, not a
> headline convergence figure. The paper reports convergence as a distribution
> over nine independent runs (median 3, range 2--4).

## Source

University of York RoboStar case-study set — externally authored, used to
control for the "authors picked case studies that flatter their tool"
concern. The original RoboStar material is published as RoboChart models;
the structured requirements specification (`requirements/requirement_all.json`)
and natural-language `system_description.md` consumed by our pipeline are our
own re-engineering of that material into the schema FORGE expects.

## Layout

```
sranger/
  system-description/system_description.md   ← what was handed to the LLM
  requirements/requirement_all.json          ← 23 structured requirements
  java/                                      ← converged Java source (Phase 2 output)
  formal-artefacts/
    dafny/SRangerController.dfy              ← Phase 5a output
    csp/robochart_controller.rct             ← Phase 5b intermediate
    csp/csp-gen/                             ← Phase 5b CSP-M scripts (42 files)
    isabelle/SRangerController_Beh.thy       ← Phase 5c output
    isabelle/ROOT                            ← Isabelle session config
```

## Reproducing verification

```bash
# Dafny (~5 s)
dafny verify formal-artefacts/dafny/SRangerController.dfy

# FDR4 (~20 s)
"<FDR4_PATH>/refines.exe" formal-artefacts/csp/csp-gen/defs/SRangerController_coreassertions.csp

# Isabelle/UTP (~23 s)
isabelle build -D formal-artefacts/isabelle -v -o timeout=600
```

## License

MIT — see [`../../LICENSE`](../../LICENSE).
