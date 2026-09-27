# Guard-vocabulary mapping — round-3 blind LRE reference spec → shipped system

Scope: maps the symbols and rows of `LRE-reference-spec.md` (drafted 2026-09-08,
source SHA-256 `aa9a0e2a27b8043e645353d20053789202922432c32ee2024e605e4edf5f149b`,
artifact vfa5c89c9, READ-ONLY) onto (a) the 11 atomic predicates of the Tier-B
abstract enumeration, (b) the shipped Java (`reference-runs/lre/java`). All
comparisons are static (source parsing + Python re-simulation with Java `double`
semantics); no Java was executed.

## 1. Spec symbols → atoms → Java

| Spec symbol | Meaning | Atom(s) | Java source |
|---|---|---|---|
| `s` | `minSafeDist` | threshold param MS | `LreConstants.minSafeDist` (=1.0) |
| `H` | `staticObsHorizDist` | HH | `LreConstants.staticObsHorizDist` (=1.0) |
| `V` | `staticObsVertDist` | VV | `LreConstants.staticObsVertDist` (=1.0) |
| `D` | `staticObsDfltVertDist` | DV | `LreConstants.staticObsDfltVertDist` (=1.0) |
| `P` = `(ds ≤ s) ∨ (depth ≤ 0)` | OPEZ | `inOpez` | `CheckOPEZ.compute()` → field `inOpez` |
| `C` = `cda < s ∧ tcpa ≥ 0` | collision | `cda_lt_ms ∧ tcpa_ge_0` | field `camActive` (LreController.java:176) |
| `vel ≤ 1` (T04, literal 1) | | `vel_le_1` | `velBelowOrAtOne` (:186) |
| `dd > 1`, `ds > 1` (T04, literal 1) | | `odc_gt_1`, `ocs_gt_1` | `odistCdynAboveOne`/`odistCstcAboveOne` (:187-188) |
| `hvel ≥ 1 ∧ hs ≤ H` (T09) | | `hvel_ge_1 ∧ hdist_le_h` | `hcmActive` disjunct 1 (:177-178) |
| `vs ≤ D` (T10) | | `vdist_le_dv` | `hcmActive` disjunct 2 (:179) |
| `vvel ≥ 1 ∧ vs ≤ V` (T11) | | `vvel_ge_1 ∧ vdist_le_v` | `hcmActive` disjunct 3 (:180-181) |
| `hs > H ∧ vs > V` (T13) | | `¬hdist_le_h ∧ ¬vdist_le_v` | field `momReturn` (:182-183) |
| `cda ≥ s` (T18) | | `¬cda_lt_ms` (exact reals) | `cdaAboveOrAtMinSafe` (:189) |

All transition guards are Boolean combinations of the same 11 atoms as the
previous enumerations; **no new abstract guard dimension is required**.

## 2. The relation itself — what changed vs the previous spec

This spec's `R(m,e,x)` is **set-valued with no priority** (A02): the set of
`(destination, outputs)` over ALL enabled rows T02–T18, with the explicit hold
row `{(m, [])}` exactly when no row is enabled. Two comparison-semantics
dimensions therefore change relative to the round-2 scoring:

1. **Membership, not equality.** Java conformance = Java's `(dest, outs)` is a
   MEMBER of `R(m,e,x)` per cell. A fixed-priority implementation "can select a
   subset of the allowed alternatives" (spec, Use-as-reference-oracle section),
   so the Java chain is checked as a refinement, never against a single selected
   row.
2. **Hold is deterministic.** When `E(m,e,x) = ∅`, `R = {(m,[])}` — falling off
   the Java chain (mode retained, no output) is the unique allowed outcome; when
   `E ≠ ∅`, hold is NOT in R ("no optional stuttering when a row is enabled"),
   so a mutant that refuses an enabled transition violates membership.

## 3. Event interface and startup

Spec `none` (a step without operator input) = the Java step in which every
`instanceof` test is false = the previous enumerations' `tick`. Operator events
map 1:1 (`reqVel(v)` → `InputEvent.ReqVel`, …). T01 (power-up → OCM, no output)
maps to the Java field initialization `currentMode = LreMode.OCM`
(LreController.java:70); the Java constructor emits nothing — T01 conforms.
Destination-entry advice is already folded into each spec row's output sequence,
matching the Java branch bodies which emit `actuator.receive(...)` inline on the
taken branch (advice is never emitted a second time on entry — same as Java).

## 4. Dimensions the spec observes that needed grid extension (concrete side)

- **A05 obstacle ties**: the spec fixes smallest-identifier tie-breaking among
  equally close obstacles. Two tie configurations (`ss_tie`, `dd_tie`:
  equidistant obstacle pairs with distinct ids) were ADDED to the concrete
  scenario grid (8,928 scenarios total vs 8,352 previously). The shipped Java
  `closestStaticIndex`/`closestDynamicIndex` iterate ascending and replace only
  on strict `<`, i.e. keep the smallest id — same rule; no divergence observed.
- **`lastVel`/`lastHdng` actuator records (A09)**: the spec declares the mode
  relation does NOT depend on them and their initial values are unspecified.
  No enumeration dimension is added; consequently the three actuator-internal
  mutants remain below R's interface (see scoring).
- **No other extension**: `none` input, hold row, and per-row output sequences
  were already dimensions of the round-2 machinery.

## 5. Documented representational deltas (disclosed, not silently bridged)

- **Caution-row merge**: Java collapses T09/T10/T11 into one `hcmActive`
  disjunction. Under set semantics this is invisible: the three rows share
  destination HCM and output `[advVel(0)]`, so the member contributed is
  identical. (A17 requires comparing destination and output sequence, both equal.)
- **CPA conventions (A07/A08/A14)**: for absent dynamic obstacles the spec
  takes `cda = F` (sentinel > every threshold), `tcpa = 0`; the Java runs the
  unbranched `CalcCPA` formula on `Double.MAX_VALUE` sentinels, which yields
  huge-but-finite values on most cells and `0/0 = NaN` exactly when the AUV's
  own velocity is zero (absence case) or relative velocity is zero (existing
  obstacle, a case A08/A14 places outside the spec's declared input domain).
  NaN makes every Java comparison false. Cells where either side's CPA
  convention decides the verdict are flagged CPA-edge and attributed to the
  ledger (A07/A08/A14), per the spec's own A08 disclaimer that it does not
  independently validate a checker's CPA convention.
- **Thresholds (A12)**: spec keeps `s,H,V,D` as parameters with default 1;
  shipped `LreConstants` declares all four = 1.0 — bindings coincide for the
  baseline. The four `LreConstants` mutants are scored with the spec bound to
  the mutant's own declared constants (DM4 calls the values defaults, i.e.
  configuration); the literal-1 alternative binding is reported as a
  sensitivity, not silently chosen. The literal `1`s in T04/T09/T11 guards are
  not configurable and stay 1 under every binding.
- **Priority (A02)**: the spec has none; the Java chain's fixed order
  (CAM > caution > OPEZ > operator within MOM/HCM) selects one member of R per
  cell. Under membership semantics this is conformance, not a delta to bridge —
  and correspondingly, order-only mutations are undetectable by this spec
  (disclosed in its closing section: "Priority-only differences may therefore
  remain undetected").
