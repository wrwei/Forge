# Differential guard check (Section 5.5)

For one controller step, the Java controller and the extracted Isabelle theory receive the same
concrete inputs, and the Java's next mode is compared with the modes the theory's operations
admit. The theory's guard and update expressions are evaluated in exact real arithmetic
(`model_eval.py`); its sensor functions return the values the Java sensor returned. Inputs put
each compared quantity on its threshold, one double either side of it and on values whose
double computation rounds across it, plus 2,000 random inputs per controller (fixed seed); each
is combined with every mode and input event. A disagreement is attributed by re-evaluating the
theory's own expressions in IEEE double arithmetic: if that agrees with the Java, the
disagreement comes from reading `double` as real, not from the translation of a guard.

| Script | Subjects |
|---|---|
| `run_lre.py` | LRE reference controller (`harness/LreHarness.java`) |
| `run_small.py` | SRanger, gas analysis, movement reference controllers |
| `run_runs.py` | converged code of LRE loop runs 1–5 (`harness/GenericLreHarness.java`) |
| `run_sr_runs.py` | converged code of SRanger loop runs 1–5 (`harness/GenericSrHarness.java`) |
| `summarise.py` | `results/*.csv.gz` -> `summary.json` |

Result: 2,184,386 steps, 18,784 disagreements, every one explained by double versus real
arithmetic (11,768 LRE steps where the Java's closest-point-of-approach computation is not
finite; 7,016 where a computed quantity rounds across a threshold).

Theories are taken from the mutation campaign's baselines (reference controllers) and from each
loop run's converged iteration; Java 21 is needed for the harnesses.
