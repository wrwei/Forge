# Run 1, iteration 1 — Isabelle verdict void (host-environment fault)

Date: 2026-10-25. Pin: 5f403301. Actor: Claude Code, cold start.

Iteration 1 passed 11 of 12 phases (compile, coverage, preflight, T2M, M2M, M2T,
Dafny generation, Isabelle generation, FDR4 9/9, Dafny both controllers, vacuity
0 blocking). `isabelle_verify` failed after 408 s with
`lualatex: command not found` while building the upstream session `ITrees`.

Cause, outside the worktree: `~/.isabelle-docker/etc/settings` on the host
carried `ML_PLATFORM="x86_64-linux"` (written by the September Steam Boiler
rerun). The container wrapper had never read that file until the 2026-10-23
wrapper fix mounted it where Isabelle looks; from then on Isabelle selected the
64-bit platform, for which the distribution ships no prebuilt heaps, and began
rebuilding the whole stack from `Pure`; the `ITrees` session's document build
needs LaTeX, which the image deliberately omits. No pinned tree was involved.

Fix: the `ML_PLATFORM` line was removed (the `--maxheap 6g` line kept) and the
partial 64-bit heaps deleted. Confirmation outside the runner: `isabelle build`
on the iteration-1 session loaded the prebuilt parents and finished
`GasAnalysisController_Check` (both theories) in 6 min 29 s.

Consequence for this run: the actor made NO change to the Java between
iteration 1 and iteration 2; iteration 2 re-judges the identical cold output and
is the first iteration with a complete verdict set. Read this run's convergence
iteration accordingly when the collection is reported.

## Provenance of the iteration-2 Isabelle verdict

The runner's iteration-2 `isabelle_verify` completed in 11 s because the session
had been built minutes earlier, from the identical theories, by the operator's
confirmation command (the host-environment check above), and Isabelle found it
up to date. To make the proof independently evidenced, the cached session was
removed and the recorded iteration-2 theories were rebuilt from scratch outside
the runner: `isabelle_iter2_independent_rebuild.log` (both theories,
`Finished GasAnalysisController_Check` in 6 min 02 s of session time, 6 min 13 s for the whole build; no `sorry`, no warnings).
The runner's verdict and this rebuild judge byte-identical sources.
