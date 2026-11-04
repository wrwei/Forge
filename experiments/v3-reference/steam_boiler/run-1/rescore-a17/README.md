# Amendment 17 re-scores: Reference Steam Boiler run 1

At the pin `aaddf222`, with Amendment 17's FDR4 settings: 81,920 MB budget on the physical
footprint, FDR4's disk store through `scripts/fdr4_paged.sh` (24 GB in-memory cache),
timeout 9000 s. Each iteration's recorded Java and trace, run once through all twelve
phases by the operator. The recorded verdicts and the run's convergence at iteration 4
stand.

## Iteration 1 (2 November 2026, 19:21-19:42 UTC)

| Phase | Recorded (40,960 MB) | Re-scored (Amendment 17) |
|---|---|---|
| profile check | failed (10 rule-4 errors) | failed (10 rule-4 errors) |
| FDR4 | stopped by the monitor at the budget (peak 41,396 MB) | stopped by the monitor at the budget (peak 81,940 MB), no verdict |
| Isabelle | deadlock-freedom timeout | deadlock-freedom timeout |
| vacuity audit | crashed ("maximum recursion depth exceeded") | crashed (same) |
| all other phases | passed | passed |

The disk store stayed empty (0 B after the run).

## Iteration 2 (2 November 2026, 19:44-20:10 UTC)

| Phase | Recorded (40,960 MB) | Re-scored (Amendment 17) |
|---|---|---|
| FDR4 | stopped by the monitor at the budget (peak 41,226 MB) | killed by signal 9 (peak seen by the monitor 81,373 MB), no verdict |
| Isabelle | deadlock-freedom tactic timeout | deadlock-freedom tactic timeout |
| all other phases | passed | passed |

The disk store stayed empty (0 B after the run). The system log records `memorystatus: killing largest compressed process _refines [70148]
81165 MB` at 19:59:58 UTC: macOS's memory-pressure killer ended FDR4, before the monitor's
budget fired.

## Iteration 3 (2 November 2026, 20:30-20:50 UTC)

| Phase | Recorded (40,960 MB) | Re-scored (Amendment 17) |
|---|---|---|
| FDR4 | stopped by the monitor at the budget (peak 41,374 MB) | killed by signal 9 (peak seen by the monitor 76,976 MB), no verdict |
| Isabelle | deadlock-freedom tactic timeout | deadlock-freedom tactic timeout |
| all other phases | passed | passed |

The disk store stayed empty (0 B after the run). The system log records `memorystatus: killing largest compressed process _refines [75640]
76745 MB` at 20:40:19 UTC: macOS's memory-pressure killer ended FDR4, before the monitor's
budget fired.
