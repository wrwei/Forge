# Reference feedback, Chemical Detector, both controllers (condition F)

The Reference Chemical Detector row of the paper's Table 8: five cold runs of
the two-controller Chemical Detector under the full Reference feedback, with the
pipeline that emits one Dafny file and one Isabelle theory per controller.
Pre-registered before any run (`preregistration.md`, with its dated amendments).

Pipeline: the published trees of this repository at `09e67fca` plus the
executable-bit fix `d88ad2a1`, copied byte-for-byte onto the research branch
`condition-f` and pinned there at `5f403301` (after a first pin, `6b78ebe8`, was
discarded for a file-mode defect; see `discarded-run1-first-pin/`). Actor: Claude
Code, autonomous, iteration cap 7, one fresh session per run, forbidden to read
`forge.transformations/` and `forge.dashboard/`; every transcript is exported
and audited (`run-<k>/TRANSCRIPT_AUDIT.md`).

| run | converged | iterations | Java files / lines | Isabelle session at convergence |
|---|---|---|---|---|
| 1 | yes | 2 (see note) | 18 / 627 | 360 s (independent rebuild) |
| 2 | yes | 1 | 19 / 563 | 356 s |
| 3 | yes | 1 | 20 / 661 | 342 s |
| 4 | yes | 1 | 20 / 653 | 321 s |
| 5 | yes | 1 | 20 / 647 | 386 s |

Per run: 22 Dafny obligations, FDR4 9/9 assertions, 86 Isabelle lemma
statements (44 named invariant-preservation and deadlock-freedom lemmas, 42
requirement lemmas), vacuity audit clean. Median (range) of iterations 1 (1–2);
cold convergence 4/5.

Note on run 1: the iteration-1 Isabelle verdict was void — a stale
`ML_PLATFORM` line in the host's Isabelle user settings, made effective by a
wrapper fix two days earlier, selected a platform with no prebuilt heaps
(`run-1/ENVIRONMENT_FAULT_iter1.md`). No pinned tree was involved. The actor
changed nothing between iterations 1 and 2 (the two staged Java trees are
byte-identical), so iteration 2 is the first complete verdict on the cold output.
The row reports the iteration as measured (2) and says so in the caption.

Layout per run, as in the other collections: `actor.json`, `setup_ok.json`,
`iter-<n>/{java,formal-artefacts,feedback,traces,summary.json,agent_summary.md}`,
plus `transcript.md` and `TRANSCRIPT_AUDIT.md`.
