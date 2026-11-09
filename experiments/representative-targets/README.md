# Representativeness checks (paper Section 5.4)

Two checks of the deterministic pipeline, with no LLM session.

* `api-io/` — the LRE reference implementation (`A0`) and two variants that add telemetry
  received over a TCP socket and parsed with Gson, and a JSON-lines command log: `A1` in
  classes of their own, `A2` inside `Sensor` and `Actuator`. Each subject has its Java and the
  codegen trace used by the coverage phase.
* `synthetic/` — `make_synthetic.py` and the five generated controllers (4, 8, 12, 16, 20
  modes; 13–77 transitions), each with requirements and a codegen trace.
* `results/<subject>/` — `run.log`, the phase reports and `phase_timings.json` (`feedback/`),
  and every generated artefact (`artefacts/`).
* `run_all.sh` — runs every phase on the subjects in a throw-away export of the repository.

Outcome: A1 and A2 pass every phase and their 66 generated artefacts are identical to A0's
apart from the CSP generator's date stamp. On the synthetic controllers both verifiers finish
at 8 modes and neither at 12 (FDR4 over its 16 GiB budget; Isabelle over 600 s).
