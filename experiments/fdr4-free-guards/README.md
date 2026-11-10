# FDR4 on the LRE with the guards left free (Section 5.6.2(i))

In the CSP that Phase 6b checks, the LRE's sensor functions are constant stubs (0) and numeric
types are narrowed to {0..1}, so only the operator-control mode (OCM) is ever entered. Here every
guard atom of the controller's state machine (a comparison, or a Boolean variable read as a
condition) may take either value at every step, so every mode can be entered. The pipeline's
three counted assertions are checked again, with one assertion per mode that it can be entered.

- `prepare.py staged` stages the 15 models (the reference controller and the converged iteration
  of the 9 converged cold passes and the 5 loop runs) with free guards; `prepare.py control
  --control` stages the same models unchanged. Each model is first given the pipeline's CSP
  corrections, as in Phase 6b.
- `run_fdr4.sh [staging] [output]` runs FDR4 4.2.7 on each model.

| | Free guards (`results/`) | As Phase 6b checks them (`control-results/`) |
|---|---|---|
| Deadlock-free; deadlock-free with termination; divergence-free | pass, 15 of 15 | pass, 15 of 15 |
| OCM entered | 15 of 15 | 15 of 15 |
| MOM, HCM, CAM entered | 15 of 15 | 0 of 15 |
