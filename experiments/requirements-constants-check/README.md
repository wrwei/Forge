# Values the requirements state (paper Section 4.9)

`tables/<study>.json` lists every value the case study's requirements state, with the
requirement that states it: named constants with a value, and the number or named constant a
quantity is compared with. `check_stated_values.py` compares one table with a Java tree and
fails an entry when a constant is declared with another value or a comparison of the quantity
uses another number. Values are compared, not operators.

* `run_campaign.py` — applies the check to the three unmutated implementations and to every
  inside-boundary mutant of `../mutation-testing/` (`results.csv`, `summary.json`): 14 of 152
  caught, all off-by-one changes (LRE 10 of 10, SRanger 4 of 6), none of the other 138.
* `run_sessions.py` — applies it to the converged iteration of each loop run on the three
  studies (`sessions.csv`): every entry found and holding in all 15.
* The Chemical Detector's requirements name its constants without values, so its table is empty.

`results_run1.csv` and `sessions_run1.csv` are a first run in which a stated value given as a
constant name was resolved through the Java instead of the table; campaign verdicts are
identical, and the 15 session differences were naming only (every value found was the stated one).
