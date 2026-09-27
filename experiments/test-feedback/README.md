# Test-and-static-analysis feedback runs (RQ5, Section 4.7)

The three SRanger runs driven by failing-test and static-analysis feedback in
place of verifier diagnostics: per-run records (`tarm_run*.tar.gz`), the JUnit 5
suite and its provenance (`sranger-requirements-junit5.zip`), the driver, the
verifier re-checks on the converged code (`tarm_formal_artefacts.tar.gz`,
`tarm_verify_*`), and the reports. The integrity check recorded zero
verifier-derived vocabulary in all feedback sent.
