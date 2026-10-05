# Case Study: Last Response Engine (LRE)

A reactive safety controller for an Autonomous Underwater Vehicle (AUV)
developed by the National Oceanography Centre. Single controller, four
operating modes — **Operator Control Mode (OCM)**, **Main Operating Mode
(MOM)**, **High Caution Mode (HCM)**, and **Collision Avoidance Mode (CAM)** —
and 13 transitions.

## Summary

| Metric                              | Value |
| ----------------------------------- | ----- |
| Controllers                         | 1     |
| Operating modes                     | 4     |
| Transitions                         | 13    |
| Requirements                        | 51    |
| Java files                          | 15    |
| Java LOC                            | 963   |
| Snapshot from iteration             | 4 (one trajectory; see note) |
| Dafny verification                  | 8 verified, 0 errors (Dafny 4.11.0 + Z3 4.15.4, re-run 2026-08-15; an earlier revision of this row said 6, predating the archived artefact's final state) |
| FDR4 (CSP)                          | deadlock-free, divergence-free, deterministic (all 4 assertions pass) |
| Isabelle/UTP build wall-clock       | ~40 s |

> **Snapshot note.** This artefact is from one converged trajectory; the
> iteration index above is that trajectory's converging iteration, not a
> headline convergence figure. The paper reports convergence as a distribution
> over nine independent runs (median 3, range 2--4).

## Sources

- Foster, Cavalcanti, Woodcock et al., *AUV safety analysis*, 2020 — original AUV controller design
- Wei et al., ACCESS 2024 — RoboChart model + Isabelle/SACM verification (reference ground truth used to cross-check our extracted model)

## Layout

```
lre/
  system-description/system_description.md   ← what was handed to the LLM
  requirements/requirement_all.json          ← 51 structured requirements
  java/                                      ← converged Java source (Phase 2 output)
  formal-artefacts/
    dafny/LreController.dfy                  ← Phase 5a output
    csp/robochart_controller.rct             ← Phase 5b intermediate
    csp/csp-gen/                             ← Phase 5b CSP-M scripts (74 files)
    isabelle/LreController_Beh.thy           ← Phase 5c output
    isabelle/ROOT                            ← Isabelle session config
```

## Reproducing verification

```bash
# Dafny (~5 s)
dafny verify formal-artefacts/dafny/LreController.dfy

# FDR4 (~30 s)
"<FDR4_PATH>/refines.exe" formal-artefacts/csp/csp-gen/defs/LreController_coreassertions.csp

# Isabelle/UTP (~40 s; runs in WSL on Windows)
isabelle build -D formal-artefacts/isabelle -v -o timeout=600
```

## License

MIT — see [`../../LICENSE`](../../LICENSE).
