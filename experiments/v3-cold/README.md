# Pre-registration v3 collections (v3-cold, v3-reference) — independence record

Design: `review/prereg-v3-study-free.md` (paper repository). Pin `5280e26e`.
Each pass or run has its own `TRANSCRIPT_AUDIT.md`. This note records the checks
across passes, made on 30 October 2026 after the first 10 sessions (cold
SRanger 1–3, LRE 1–2, Chemical Detector 1–2; Reference SRanger, LRE and
Chemical Detector 1).

- **No channel between sessions in the records.** Every session ran in its own
  freshly set-up worktree, had one user turn (the prompt), read only its study's
  inputs, the shared instructions and its own Java, and opened no other
  worktree, staging folder, archive, reference run, report or git history.
- **No overlap in time.** Each session was set up after the previous session's
  pipeline had finished (gaps of 4 min to 9.5 h).
- **Identical harness text.** The only hook, the `superpowers` SessionStart hook,
  injected the same text in all 10 sessions (SHA-256 prefix `c0b542fbe3cb`). No
  Skill call in any session. Claude Code 2.1.312 and `claude-opus-5-5`
  throughout.
- **No shared memory.** No user-level `~/.claude/CLAUDE.md`; no memory files in
  any v3 session's project folder. Files under `~/.claude` changed during the
  collection are Claude Code's locks, session registry and plugin/MCP caches,
  none of which is read into a session.
- **Code similarity** (line similarity of comment-free Java, `difflib`), compared
  with the earlier cold collection (same model, previous instructions) and with
  each study's reference run, which no session could read:

  | Study | v3 pairs | cold-baseline-v2 pairs | v3 vs reference run |
  |---|---|---|---|
  | SRanger | 0.79–0.90 (6 pairs) | 0.73–0.86 (10) | 0.40–0.43 |
  | LRE | 0.69–0.75 (3) | 0.61–0.80 (10) | 0.46–0.49 |
  | Chemical Detector | 0.50–0.69 (3) | 0.35–0.52 (10) | 0.11–0.12 |

  Only the LRE range lies inside the earlier one. For SRanger one pair (cold
  pass 2 vs Reference run 1, 0.90) exceeds the earlier maximum; the other five
  are within it. The Chemical Detector ranges overlap (v3 minimum 0.50, earlier
  maximum 0.52), but the v3 range sits higher overall (the v3 rules fix more of
  the structure). Every session is far from the
  unseen reference run.
- **Open decisions differ.** SRanger distance with no reading: 100.0 in two
  sessions, 1000.0 in two. LRE CPA divide-by-zero guard: 0.000000000001 in one,
  0.000001 in two as a named constant, 0.000001 inline in one (cold pass 3, added
  30 October). Chemical Detector: 18 or 19 files with differently named
  helper classes. The SRanger clock field is `timer` in every session because
  the rules file gives `clock` -> `timer` as its renaming example (line 93).
- **Shared by design:** the model and the instructions (ruled not to count
  against independence; stated in the paper).

## The verifiers ran (spot check, 30 October 2026, on cold LRE pass 3)

Checked because the sessions finish quickly. All three verifiers were re-run by
hand on that pass's recorded artefacts:

- **Dafny 4.11** (`dafny verify`, the phase's own command): `12 verified, 0
  errors` in 0.86 s, matching the phase's 0.87 s; with one postcondition replaced
  by `ensures false`, `a postcondition could not be proved` (11 verified, 1
  error). Dafny is a native build; the models carry 7 to 23 obligations.
- **FDR4** (`refines --brief`, academic licence, the operator's terminal) on the
  file the phase checks (`defs/LreController_coreassertions.csp`, its
  determinism assertion removed as the phase does): deadlock freedom, deadlock
  freedom up to termination and divergence freedom all `Passed`; an added
  `assert STOP :[deadlock-free]` `Failed` with a counterexample. Up to 20,287
  states and 176,412 transitions explored; 9.4 s in total, against the phase's
  10.1 s. The "3 assertions" the phase reports are these three.
- **Isabelle** (`scripts/isabelle-docker.sh build -D <copy> -v -o timeout=600`,
  the phase's own command, the operator's terminal) on a copy of the recorded
  theory: `Finished LreController_Check` in 2 min 3 s (137.0 s wall clock, against
  the phase's 136.7 s), all 47 lemmas proved. On a second copy with
  `lemma planted_false: "(1::nat) = 2" by simp` added before `end`: `Failed to
  finish proof (line 680)`, remaining goal `False`, after 135.7 s, i.e. after the
  47 real lemmas had been checked. Across all 11 sessions the number of lemmas
  Isabelle reports verified equals the number in the theory, no theory contains
  `sorry` or `oops`, and the phase time grows with the theory (about 27 s for
  SRanger, 2–2.5 min for the LRE, about 4 min for the Chemical Detector's two
  theories). Each pass's theory differs, so no build can be reused from an
  earlier pass.

## Amendment 2 (30 October 2026)

After cold Chemical Detector pass 3 met an extractor defect (a function called
inside an operation-call argument was never declared), the extractor was fixed
under Amendment 2 of the pre-registration and the pin moved from `5280e26e` to
`2db4c031`. The regression over all 308 reported Java sources
(`experiments/regression-2026-10-30-call-argument-functions/`) leaves every
other source's artefacts unchanged, so the eleven other sessions keep their
verdicts. Pass 3 was re-scored at the new pin and converges; its verdict at the
old pin is kept in its audit. Every session from row 13 on is set up at
`2db4c031`. The independence checks above are unaffected: the change is to the
extractor, which no session reads.

## Amendment 3 (30 October 2026)

- **Files removed by setup from row 15 on.** The root `README.md`, `docs/` except
  `docs/corpus/`, `.claude/memory/`, `.claude/commands/`, `.claude/skills/`,
  `scripts/envmodel_b/`, and the comment lines of `pipeline.yaml` that name a
  study. Each names the studies; none is on the reading list or used by the
  pipeline. No session so far read any of them: every exported transcript was
  searched for each path, the full command log of cold Chemical Detector pass 4
  likewise, and each earlier audit lists its session's reads from its full command
  log. A search of all fourteen sessions' full command logs (operator's
  terminal, 30 October) finds one match, `cd …/docs/corpus && cat README.md` in
  Reference LRE run 1: the permitted corpus README.
- **Order.** Cold SRanger pass 4 was skipped by operator error; cold LRE pass 4 and
  cold Chemical Detector pass 4 ran one place early. SRanger pass 4 runs next.
  `scripts/v3_start.sh` now starts only the next session in the order.
- **IDE notice.** In cold Chemical Detector pass 4 the VS Code extension told the
  session which file was open in the editor (the root `README.md`); the session
  did not open it. The start script now reminds the operator to close every tab
  before pasting the prompt.

## Amendment 4 (30 October 2026)

Amendment 3's removal of `docs/` deleted the pipeline's manifest schema
(`docs/superpowers/specs/pipeline-manifest.schema.json`), so in cold SRanger pass
4, the first session set up under it, the runner stopped before any phase (12
phases without a verdict). Setup now keeps that file and refuses a worktree in
which the pipeline cannot load its manifest. Pass 4's iteration is re-run by the
operator on its unchanged Java and trace (`scripts/v3_setup_fault_rerun.sh`); the
faulted record is kept as `iter-1-setup-fault/`.

## Profile-checker false positive (30 October 2026, Reference Chemical Detector run 2)

Iteration 1 of Reference Chemical Detector run 2 failed only the profile check, on
rule 4 ("field `GasSensor.i` is a double without `@RoboChartType(\"real\")`"),
although the record component carries the annotation:
`public record GasSensor(Chem c, @RoboChartType("real") double i) { }`. Cause: the
checker inspects the implicit field of each record component, and the Java parser
(Spoon) copies the component's annotation onto that field only when the record is
not in the source root's top-level package; reproduced with a one-line record in
`p` (not copied, false error) and in `p.domain` (copied, passes). Every other
Chemical Detector session placed its reading record in a sub-package. Decision
(operator, 30 October): **the pin is kept**; the run is reported as converged at
iteration 2, the iteration-1 failure as a profile-checker false positive. At
iteration 2 the session changed intensity from `double` to `int` (`nat`) in four
files (`ChemicalDetectorConstants`, `GasSensor`, `GasAnalysisController`,
`ChemicalSensor`), which the requirements allow (intensity need only be totally ordered),
and every phase passed.

## Claude Code version change (30 October 2026)

Claude Code updated itself from 2.1.312 to 2.1.314 between Reference Chemical
Detector run 2 (row 18, 2.1.312) and cold SRanger pass 5 (row 19, 2.1.314). The
model ID (`claude-opus-5-5`) and the startup-hook text (hash `c0b542fbe3cb`) are
unchanged. Disclosed, as the 2.1.311 to 2.1.312 update in cold-baseline-v2 was;
each session's version is recorded in its audit.

## Two void sessions (30 October 2026)

`pipeline.yaml` is not on the pre-registered reading list, so reading it beyond
the prompt's preflight line is a forbidden read and voids the session (Validity
section). Two sessions did: cold LRE pass 5 (`cat pipeline.yaml | head -40`) and
Reference LRE run 1 (`grep -n -A5 'agent' pipeline.yaml | head -30`; its audit had
missed the read). Neither read names a study. Both are void, by the operator's
decision to apply the rule as written; their records are kept and not counted.
They are replaced by cold LRE pass 11 and Reference LRE run 6, run after the
order; their prompts were generated with the same generator (they differ from
passes 10 and 5 only in the number) and committed before they ran.
`scripts/v3_start.sh` places replacements after the Steam Boiler run. Every audit
now checks for `pipeline.yaml` reads explicitly.

**Check across all twenty sessions (30 October, operator's terminal).** Every
mention of `pipeline.yaml` in every v3 session's full command log, other than the
prompt's preflight line, was read. Only the two void sessions read the file's
contents. The other mentions read nothing from it: `find … -newer pipeline.yaml`
(cold LRE pass 1, which compares file times) and `git status -- … pipeline.yaml …`
(cold SRanger passes 3, 4 and 5, cold Chemical Detector passes 2 and 3, and the
void LRE pass 5, which check whether the protected paths changed).

## Amendment 5: the two void declarations withdrawn (30 October 2026)

Cold Chemical Detector pass 5 also read `pipeline.yaml` (`grep -n -A3 'agent'
pipeline.yaml`). Looking for why, the study-free `CLAUDE.md` (line 14) and layer
prompt `01_architecture.md` (lines 8–9), both on the reading list, were found to
tell the session that `pipeline.yaml`, key `agent.active_case_study`, names the
study: the reading list and the instructions on it contradicted each other, and
all three sessions read the file while looking up their study. Amendment 5 makes
reading `pipeline.yaml` a permitted read (writing it still voids). The void
declarations of cold LRE pass 5 and Reference LRE run 1 are withdrawn (kept as
`VOID_WITHDRAWN.md`); both are valid; the replacement prompts (cold LRE 11,
Reference LRE 6) are withdrawn unused. The earlier declaration was made before the
cause was looked for; that order of events is reported.

## Amendment 7: the scaffold named another study (31 October 2026)

Cold LRE pass 8's session received a VS Code diagnostic naming the project
`chem-detector-java.generated.project`. The Gradle scaffold every session reads
named the Chemical Detector (`rootProject.name = 'chem-detector'`, `group =
'chemdetector'`); the leakage gate had not scanned it. Every other file a session
may read was scanned for study names and carries none. Amendment 7 neutralises the
scaffold (pin `56cb43d7`), extends the gate to the scaffold and `pipeline.yaml`
(five new test cases; 25 of 25 pass), and gives new sessions new worktree and
staging names. The 22 SRanger and LRE sessions collected before it are superseded
and kept in `experiments/v3-superseded-a7/`; both studies are re-collected from
pass 1. The ten Chemical Detector sessions stand: their scaffold named only their
own study. The independence checks above were made on sessions that include the
superseded ones and are repeated on the re-collection.

## Amendment 8: `.gitignore` and a `pipeline.yaml` comment named studies (31 October 2026)

Cold SRanger pass 2 (re-collection) received an IDE notice with the path of
`.gitignore`, open in the VS Code window; the session did not read it. The file
names SRanger and LRE (four lines), and one `pipeline.yaml` comment
(`LreController_Check …`) had escaped Amendment 3's removal. No session in the
collection read either. Setup now removes these lines
(`scripts/v3_strip_study_names.sh`), and the leakage gate scans `.gitignore` and
all of `pipeline.yaml` (29 of 29 test cases pass). Pin unchanged. Cold SRanger
passes 1 and 2 and cold LRE pass 1 were set up before the change.
