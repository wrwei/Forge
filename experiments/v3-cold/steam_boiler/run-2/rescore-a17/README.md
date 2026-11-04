# Amendment 17 re-score: cold Steam Boiler pass 2, iteration 1

At the pin `aaddf222`, with Amendment 17's FDR4 settings: 81,920 MB budget on the physical
footprint, FDR4's disk store through `scripts/fdr4_paged.sh` (24 GB in-memory cache, store
in `/Users/ranwei/Gitee/fdr4-store`), timeout 9000 s. Run by the operator on 2 November
2026, 18:51-19:13 UTC.

| Phase | Recorded (40,960 MB) | Re-scored (Amendment 17) |
|---|---|---|
| profile check | failed (rule 4) | failed (rule 4) |
| FDR4 | stopped by the monitor at the budget (peak 40,982 MB) | killed by macOS (signal 9), no verdict |
| Isabelle | deadlock-freedom timeout | deadlock-freedom timeout |
| all other phases | passed | passed |

FDR4 was ended by the operating system, not by the pipeline: the system log records
`memorystatus: killing largest compressed process _refines [61407] 80743 MB` at
19:03:27 UTC; the pipeline's monitor had seen a peak of 80,951 MB. FDR4's last output was
its compilation progress ("Found 18000 processes including 87 names") and no assertion
result; the disk store stayed empty (0 B, checked by the operator during and after the
run). So the memory went into compiling the model, before state exploration, where the
disk store would apply.

The counted verdict of the pass does not change (its first failing phase is the profile
check). An earlier attempt, in which FDR4 rejected the cache option and did not run, is in
`../rescore-a17-attempt1-option-rejected/`.
