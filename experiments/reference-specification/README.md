# Independently authored reference specification (Section 4.9.1)

The LRE reference specification written from the published requirements document
alone, without access to any implementation artefact (`LRE-reference-spec.pdf`, with
its source `.tex`; the requirements file it was written from, `lre_requirement_all.txt`),
the conformance check of the converged implementation against it
(`conformance.csv`, 57,344 abstract cells, 100% membership), and the
scoring of the 54 LRE mutants that survive every verifier of the mutation-testing
experiment (`survivor_scoring.csv`: 32 killed, every kill unambiguous).
`report.md` is the analysis report; `guard_mapping.md` maps the
specification's vocabulary to the system's.

Reproduce the scoring from the repository root (Python 3 with numpy):

    python3 experiments/reference-specification/score.py --check
