# Cold baseline, re-scored by the published extractor

The 30 cold single-pass Java sources of `../cold-baseline/` (ten per case study,
generated once each with no feedback of any kind), each run through the published
pipeline exactly once, all twelve phases, verifiers included. This is the cold
baseline the paper's Section 4.5 (RQ3) and Tables 5 and 6 report. The submitted
paper had scored the same sources with the extractor of that time; its verdicts
remain in `../cold-baseline/` and are discussed in the response letter.

- `preregistration.md` — written before any pass ran: what is replayed, what is
  recorded, how the paper is updated whatever the counts, and the amendment made
  after the first complete pass (a Phase 6d double report, an FDR4 memory cap).
- `<study>/run-N/` — one pass: `phase_status.json` (the twelve verdicts, the
  extractor commit, elapsed time), `pipeline-results.md`, `feedback/` (every
  per-phase report), `formal-artefacts/` (the Dafny, RoboChart, CSP and Isabelle
  artefacts generated from that source), `driver.log`.
- `superseded-d88ad2a1/` — the first-pass records of the 14 passes rerun after
  the amendment, kept as evidence; the aggregator does not read them.
- `RESULTS.md`, `results.csv`, `best_of_n.csv` — the aggregate
  (`../scripts/replay_aggregate.py`), including the best-of-N selection table.
- `replay.log` — the run log of both passes (31 passes at `d88ad2a1`, the 14
  reruns at `e7775bbf`).

Result: 11 of 30 converge (SRanger 4, LRE 7, Chemical Detector 0). Of the other
19, 12 fail on a verdict about the code and 7 on a generation failure of the
pipeline itself; `RESULTS.md` and the paper's Table 5 give the split.
