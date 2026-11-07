# Experiments

The records behind Section 4 of the paper. Every session's Java, generated artefacts
and per-phase verifier reports are kept as recorded; each folder's README says what it
holds and how its figures were computed.

| Folder | Paper | Contents |
| --- | --- | --- |
| [`v3-cold/`](v3-cold/) | Section 4.5 (RQ3), Tables 5 and 6; Section 4.10 (RQ8), Table 10 | 40 cold passes: 10 per case study, including the Steam Boiler |
| [`v3-reference/`](v3-reference/) | Section 4.6 (RQ4), Table 7; Section 4.10 (RQ8), Table 10 | 20 loop runs: 5 per case study, every iteration recorded |
| [`v3-compile-only/`](v3-compile-only/) | Section 4.7 (RQ5), Table 8 | 15 loop runs in which the verifiers' reports are withheld from the session |
| [`v3-analysis/`](v3-analysis/) | Tables 5–8, 10; Section 4.11 | the analysis scripts' outputs: per-session verdicts, best-of-N, convergence, token usage, the Steam Boiler requirement split |
| [`v3-preregistration/`](v3-preregistration/) | Section 4.2 | the design of the collection with its dated amendments, the prompt given to every session, and the set-up, runner, recorder and audit scripts |
| [`mutation-testing/`](mutation-testing/) | Section 4.8 (RQ6), Table 9 | 186 mutants of the three RoboStar studies through every verifier; manifest, generator, kill table |
| [`requirements-conformance/`](requirements-conformance/) | Section 4.9 (RQ7), Figure 2 | the compiled requirements relations for the three studies, conformance of the shipped controllers, survivor scoring |
| [`blind-spec/`](blind-spec/) | Section 4.9.1 | the reference specification of the LRE written from the requirements alone, and its conformance and survivor scoring |
| [`conformance-tierB/`](conformance-tierB/) | Section 5.5 | guard-level enumeration of the LRE model against its Java |
| [`final-mode-divergence/`](final-mode-divergence/) | Section 4.4 (RQ2) | a divergence between the CSP and Isabelle artefacts of one controller, and its repair |
| [`environment-model/`](environment-model/) | Section 4.4 (RQ2) | the environment-model correction and the theories proved under it |
| [`test-feedback/`](test-feedback/) | response letter | test-and-static-analysis feedback in place of verifier feedback |
| [`steam-boiler/`](steam-boiler/) | Section 2.4 | the Steam Boiler specification as used by the pipeline, and the first run on it |
| `regression-*/` | response letter | for each pipeline change after the first collection, every recorded Java source and every mutant re-extracted at the revision before and after |

## Reproducing the pipeline's verdicts

Each session folder holds the Java the session wrote (`iter-N/java/`), the artefacts the
pipeline generated from it (`iter-N/formal-artefacts/`) and the reports of every phase
(`iter-N/feedback/`). To re-run the pipeline on a recorded source, copy its `java/` into
`java.generated.project/src/main/java/` and run the pipeline as described in the
repository README; the verifier versions are listed there.

The LLM sessions themselves cannot be replayed: each was an interactive Claude Code
session whose transcript is in the session folder (`transcript.md`) with an audit of what
it read and wrote (`TRANSCRIPT_AUDIT.md`).
