# Independently authored reference specification (Section 4.9.1)

The LRE reference specification written from the published requirements document
alone under the documented isolation protocol (`LRE-reference-spec.md`, `.tex`,
`.pdf`; the requirements file it was written from, `lre_requirement_all.txt`),
the conformance check of the converged implementation against it
(`blindspec3_conformance.csv`, 57,344 abstract cells, 100% membership), and the
scoring of the 54 LRE mutants that survive every verifier of the mutation-testing
experiment (`blindspec3_survivor_scoring.csv`: 32 killed, every kill unambiguous).
`blindspec3_report.md` is the analysis report; `spec3_guard_mapping.md` maps the
specification's vocabulary to the system's.

Reproduce the scoring from the repository root (Python 3 with numpy):

    python3 experiments/blind-spec/blindspec3_score.py --check
