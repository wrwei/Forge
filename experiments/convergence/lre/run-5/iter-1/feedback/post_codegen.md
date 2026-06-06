# Post-Codegen Review — LRE iter 1 (cold)

**Status:** complete — full package generated (15 files under `lre/`), all 52 requirement IDs traced in `result_codegen.json`.

## Issues for user review

1. **[ambiguous_requirement] CAM has no defined entry action.** LRE-FR4 says CAM "performs evasive manoeuvres", but the only outputs are advVel/advHdng (LRE-DM7) and no requirement specifies a CAM entry output. Transitions into CAM currently set the mode only.

2. **[invented_default] CPA math.** LRE-OP5 gives no formula. Implemented standard 2D horizontal CPA: `tcpa = -(r·v)/(v·v)`, `cda = |r + v·tcpa|` for `tcpa > 0`, else current horizontal distance. Zero relative speed (incl. no dynamic obstacle): `tcpa = -1.0`, `cda = sensor.hdist(cdyn)` — the safe large distance when no obstacle exists.

3. **[design_choice] Beh4 thresholds.** Used literal `1.0` for the `odist > 1` guards per the requirement text, not `minSafeDist` (which happens to equal 1).

4. **[design_choice] Var1..8 live on operations.** `inOpez`/`hvel`/`vvel`/`vel`/`cstc`/`cdyn`/`cda`/`tcpa` are fields of the operation classes (per the operation pattern), read by the controller's named predicates each step — not duplicated as controller fields.

## Next step

Run the pipeline and iterate on `post_*` feedback.
