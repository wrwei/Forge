# Amendment 17 re-score: cold Steam Boiler pass 1, iteration 1

At the pin `aaddf222`, with Amendment 17's FDR4 settings: 81,920 MB budget on the physical
footprint, FDR4's disk store through `scripts/fdr4_paged.sh` (24 GB in-memory cache),
timeout 9000 s. Run by the operator on 2 November 2026, 21:05-21:21 UTC.

| Phase | Recorded (40,960 MB) | Re-scored (Amendment 17) |
|---|---|---|
| FDR4 | stopped by the monitor at the budget (peak 41,402 MB) | stopped by the monitor at the budget (peak 81,994 MB), no verdict |
| Dafny | `INITIALIZATION` not a unique datatype constructor (two enums share value names) | same |
| Isabelle | cannot type `ModeValue.DEGRADED` (same cause) | same |
| all other phases | passed | passed |

The disk store stayed empty (0 B after the run). The counted verdict of the pass does not
change (not converged; first failing phase FDR4, class (e)).
