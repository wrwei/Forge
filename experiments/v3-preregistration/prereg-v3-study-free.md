# PRE-REGISTRATION v3 — cold passes and the Reference loop under study-free instructions

Recorded 29 October 2026, before any pass or run of the collections below.

## Why

An audit of every file a generation session reads found that the instructions
carried content from the three evaluation studies: names, identifiers, examples
and rules written after earlier runs on those studies
(`review/generic-instructions-inventory.md`). The pass prompts forbade reading the
reference runs, and no audited pass did, but the instructions themselves were an
indirect channel from earlier runs to every later one. The authors decided that
the instructions must contain only generic rules. The cold-baseline-v2
collection was stopped for this reason after 15 passes (Amendment 4 of
`review/prereg-cold-baseline-v2.md`), and every compared result the paper reports
is re-collected here under study-free instructions.

## The instrument

One commit for every study and every condition: research repository commit

    5280e26e828cec41338485a44f71433436c7b55e

It is the published pipeline (Forge `9951d8a4`) with:

- study-free instructions: the root `CLAUDE.md` (the developer notes moved to
  `docs/DEVELOPING.md`, which no session reads), `forge.assets/prompts/` and
  `forge.assets/vibe-coding-prompts/`, approved by the authors before this
  pre-registration;
- study-free feedback text in three pipeline files (profile-checker fix
  directives, one vacuity message, the comment above the Isabelle
  deadlock-freedom lemma of a machine with a terminal state), shown not to change
  any verdict (`experiments/regression-2026-10-29-instructions/`);
- the Steam Boiler's requirements as a case study
  (`forge.assets/case-studies/steam_boiler/`, byte-identical to those of the RQ8
  run: SHA-256 `2e629009…` for the JSON, as in its September pre-registration);
- a corpus README (`docs/corpus/README.md`) that no longer points to the
  archived study models or to the branches holding converged runs.

Setup removes from every worktree `docs/archive/` (it holds RoboChart models and
Isabelle theories of the LRE and the Chemical Detector, which earlier collections
left readable apart from the active study's own files) and every case study
except the active one; the pipeline reads only the active study.

Setup refuses a worktree whose instructions, corpus README or feedback text
carry any study's name, declared identifiers or domain words, or that still holds
`docs/archive/` or another study's folder (`scripts/instruction_leak_gate.py`,
run for every collection named `v3-*`; tested on 20 planted cases in
`scripts/test_instruction_leak_gate.sh`).

Because the commit is the published pipeline, each pass or run has one verdict:
the one at this commit. There is no separate "at the pin" verdict. (Amendment 2
replaces this commit with one carrying a single extractor fix.)

## Actor

Claude Code, one fresh session per pass or run, never reused; Claude Opus 5.5,
checked for every response from the session log at the audit; the Claude Code
version is recorded from the same log. One pass or run at a time on this machine.

## Collections

| Collection | Condition | Studies | N | Iterations |
|---|---|---|---|---|
| `v3-cold` | Cold: one single pass | SRanger, LRE, Chemical Detector | 10 each | 1 |
| `v3-reference` | Reference loop: full feedback | SRanger, LRE, Chemical Detector | 5 each | up to 7 |
| `v3-reference` | Reference loop: full feedback | Steam Boiler | 1 | up to 7 |

**A single pass** is one session that writes the Java from the inputs, may compile
it and review it against the instructions (as the five layer prompts require),
runs the pipeline exactly once through the runner, and stops. It reads no
pipeline report, and its Java does not change after the runner starts.

**A Reference run** is one session that writes the Java, runs the pipeline through
the runner, and, while any phase fails, reads the report of each failing phase
(`forge.assets/corrections/post_<phase>.md`), changes its Java and runs again, up
to iteration 7. Iteration 1 of a Reference run is generated exactly as a single
pass is.

Not re-run: the Diagnosis-only (D2, E) and compile-only ablations. The paper no
longer reports them; the response letter reports them as results under the
previous instructions. `review/prereg-opus55-rerun.md`, under which no run was
started, is superseded by this document.

## Inputs a session may read

- `forge.assets/case-studies/<study>/system/system_description.txt` (absent for
  the Steam Boiler, whose requirements are its whole input);
- `forge.assets/case-studies/<study>/requirements/requirement_all.json` and the
  `README.md` beside it;
- `CLAUDE.md` and the files it names in `forge.assets/prompts/`, and the five
  layer prompts in `forge.assets/vibe-coding-prompts/`;
- `docs/corpus/` (the Z-Machine framework and generic examples from the
  Isabelle/UTP distribution);
- its own Java, and, in a Reference run, its own
  `forge.assets/corrections/post_<phase>.md` reports.

Everything else is forbidden, in particular: `reference-runs/`, `experiments/`,
`docs/` outside `docs/corpus/`, `forge.transformations/`, `forge.dashboard/`, the
source of `scripts/`, any other case study, any other worktree, the staging
directories, and the git history.

## Prompts

One prompt per pass or run, generated by `review/make_v3_prompts.py` from one
template per condition, differing only in study, run number and the derived
paths; the script checks this and the leakage gate is run on every prompt. The
prompts are committed with this document, before any pass.

## Order

Interleaved by number, one at a time: cold SRanger k, cold LRE k, cold Chemical
Detector k, for k = 1..10, with Reference SRanger j, LRE j, Chemical Detector j
after cold round 2j (j = 1..5); the Steam Boiler run after Reference round 5.

## Scoring

All twelve phases at the commit above, requirements coverage included (each
pass's recorded code-generation trace). A pass or run **converges** when all
twelve pass. Environment outcomes (an external kill, a host or Docker fault, a
tool timeout from machine load) are not verdicts: the iteration is re-run on a
quiet machine. The FDR4 budget is that of setup (9000 s, 10240 MB).

## Validity

A pass or run is **void** if its transcript or session log shows a forbidden
read, a write to the instrument (the pipeline, the instructions, `pipeline.yaml`,
a case study's inputs), a non-fresh session, a model other than Opus 5.5, more
than one runner call in a pass, a report read in a pass, or Java changed after
the runner started in a pass. A void pass or run is recorded with its reason and
replaced by the next number of the same study and collection (11, 12, … for
cold; 6, 7, … for Reference), run after the order above.

## Analysis, fixed in advance

1. **Cold (Table 5):** converged count per study; each non-converged pass counted
   once, under the first phase that fails in pipeline order, with the raw
   message, and classified as (a) a verdict on the code (a verifier refutes it,
   it does not compile, the profile checker or coverage rejects it); (b) Java
   outside the written instructions that no check detects, citing the rule; or
   (c) a pipeline defect on Java within the instructions.
2. **Reference (Table 7):** converged count, iterations to convergence (a run at
   the cap counts as not converged), per-phase verdicts per iteration, and the
   repairs each run made.
3. **Best-of-N (Table 6):** for N = 1, 2, 3, 5, 10, groups formed within a study
   in run order (best-of-3 leaves pass 10 unused), counted converged if the group
   holds a converging pass, under the oracle-free selector (any converging pass)
   and the verifier-informed selector (the pass clearing most phases in pipeline
   order, the earlier on a tie). The matching budget is N = the median number of
   iterations the 15 Reference runs used, rounded up, a run at the cap counted as
   7. Each N is compared with the Reference converged count by a one-sided Fisher
   exact test; all N are reported.
4. **Same-condition check:** the cold converged rate per study against the
   Reference runs' iteration-1 rate, side by side, no test.
5. **Steam Boiler (RQ8):** converged or not, iterations, per-phase verdicts per
   iteration, and every failure classified as in 1; no test.

## Amendment 1 (29 October 2026, after cold LRE pass 1, before any re-run)

**Trigger.** Cold LRE pass 1: eleven phases passed; the Isabelle phase stopped
with Docker's exit status 125 and the line `error waiting for container:
unexpected EOF` while checking the theory, i.e. the container running Isabelle
died. No proof failed. This is an environment outcome under the Scoring section.

**How an environment outcome is re-run.** The actor is not involved, so a cold
pass stays a single pass and a Reference run's trajectory is unchanged. The
operator re-runs only the phase that met the fault, on the recorded inputs of
that phase, with the pipeline's own command and limits at the pin, on a quiet
machine (no other session or verifier running), using
`scripts/v3_rerun_phase_isabelle.sh`. For the Isabelle phase the inputs are the
recorded theory and `ROOT` (`formal-artefacts/isabelle/`), which the phase reads
and nothing else; the command is the pipeline's
(`scripts/isabelle-docker.sh build -D <session> -v -o timeout=600`, 1800 s wall
clock). The re-run's log is stored beside the original report, which is kept
unchanged, and the re-run's result is that phase's verdict. If the re-run meets
an environment fault again, it is repeated; three faults in a row are reported
as such and the phase counted as having no verdict.

For a Reference run, an environment fault at an iteration the actor has already
acted on cannot be undone; it is recorded with the iteration and reported.


## Amendment 2 (30 October 2026, after cold Chemical Detector pass 3, before the pipeline change)

**Trigger.** Cold Chemical Detector pass 3 failed CSP generation (`Couldn't
resolve reference to NamedExpression 'evasionAngle'`), so FDR4 had no model; the
other ten phases passed. The Java calls a sensor method inside the argument of a
multi-argument actuator call in an entry action, `vehicle.move(LV,
sensor.evasionAngle(l))`, which the extractor turns into a RoboChart operation
call. The extractor declares a function only when it finds the call in a guard or
on the right-hand side of an assignment: the function-name collector of
`java2robochart.etl` (`collectFunctionNamesFromAction`) descends into statement
sequences and assignments and into no other statement kind, so a function called
inside an operation call, a `wait` or an output event is used in the model but
never declared. No written instruction restricts what an entry action may call,
and the profile checker accepted the code. This was established before this
amendment on a copy of the extractor outside the repositories: storing the value
in a field first (`a = sensor.evasionAngle(l); vehicle.move(LV, a);`) yields the
declaration; and the change below, applied to that copy, leaves the generated
artefacts of the other eleven collected sessions identical and adds, for pass 3,
only `function evasionAngle ( l : Loc ) : Angle { }` to the RoboChart model and
one `consts` line to each Isabelle theory.

**The change.** The collector also descends into every other statement kind and
collects function calls from every expression the statement contains. Nothing
else changes: not the instructions, the feedback text, the profile checker, the
other generators or any verifier.

**Regression, before the change is committed.** Every Java source the paper or
letter reports (the twelve sessions collected so far, cold-baseline-v2, the
Table 7, Diagnosis-only and condition-F runs, the published reference runs, the
mutation campaign's subjects, the Steam Boiler run) is extracted with and without
the change, each phase in its own process as in the pipeline. The change is kept
only if every generated artefact is identical, up to the order of set elements
and of declarations, apart from added declarations of functions called inside an
operation call, a `wait` or an output event. Any other difference stops it.

**New instrument.** The research repository commit that carries only this change
on top of the instrument above (the same change is published in Forge). Its SHA
is written into setup and into the line below once committed. Every session from
cold LRE pass 4 (row 13) onwards runs at it.

**The twelve sessions already collected.** Each is re-extracted at the new
commit from its recorded Java and trace. Where every generated artefact is
identical up to ordering, the verifiers' inputs are unchanged and the recorded
verdicts stand. Where an artefact differs (only cold Chemical Detector pass 3,
by the regression above), the phases from CSP generation on are re-run on the
regenerated artefacts with the pipeline's commands, in the operator's terminal,
and their result is the primary verdict. Its verdict at `5280e26e` (not
converged: CSP generation, a pipeline defect on Java within the instructions) is
reported beside it.

New instrument commit: research repository

    2db4c03116e00b82c1c4186c171a50ad26574e33

Against `5280e26e` its instrument differs only in this change and its test
(`experiments/regression-2026-10-30-call-argument-functions/`: 307 of the 308
sources unchanged; cold Chemical Detector pass 3 gains the three declarations).
The prompts change only in the `FORGE_SHA` value; the twelve sessions already
collected used the prompts as committed in `27a975c`.

## Amendment 3 (30 October 2026, after cold Chemical Detector pass 4, before the setup change)

**Trigger.** In cold Chemical Detector pass 4 the VS Code extension added a
notice naming the worktree's root `README.md` ("the user opened the file … in the
IDE"); the session did not open it. The root `README.md` describes the LRE as
the running case study. A scan of a whole worktree then found further files,
outside the reading list and outside the pipeline's needs, that name the
studies: `docs/` outside `docs/corpus/` (`docs/README.md`, `docs/DEVELOPING.md`,
`docs/requirements/`, `docs/superpowers/`), `.claude/memory/`,
`.claude/commands/` and `.claude/skills/` (developer notes and slash commands;
their bodies cite LRE requirement identifiers), `scripts/envmodel_b/` (the
environment-model experiment: extracted theories of all three studies and the
Steam Boiler), and two comment lines in `pipeline.yaml`. No session has read any
of them, on three checks: every exported transcript of the fourteen sessions was
searched for each of these paths (the only matches are the layer prompts'
`README.md`, `docs/corpus/README.md` and the IDE notice); the full command log of
cold Chemical Detector pass 4 was searched likewise; and each earlier session's
audit lists its reads, taken from its full command log, all within the reading
list. A search of all fourteen sessions' full command logs (30 October, in the
operator's terminal) finds one match, `cd …/docs/corpus && cat README.md` in
Reference LRE run 1, which is the permitted corpus README. No pipeline
step reads them: the pipeline uses only `scripts/isabelle-docker.sh` from
`scripts/`, and `.claude/commands/` only in the dashboard's browser view, which
no session or runner uses.

**The change.** For every `v3-*` collection, setup also removes the root
`README.md`, everything in `docs/` except `docs/corpus/`, `.claude/memory/`,
`.claude/commands/`, `.claude/skills/` and `scripts/envmodel_b/`, and deletes the
comment lines of `pipeline.yaml` that name a study (setup already edits that file
for the study and the FDR4 budget). Setup's verification refuses a worktree in
which any of these is present. The pipeline source trees (`forge.transformations/`,
`forge.dashboard/`, `experiments/scripts/` and the rest of `scripts/`), whose
comments name studies, stay in place because the pipeline runs them, and stay
forbidden to read, as above. The instrument is unchanged: no file the pipeline
reads is touched, so no verdict can change.

**Order deviation, disclosed.** Cold SRanger pass 4 (row 13) was skipped by
operator error: after cold Chemical Detector pass 3 the next session started was
cold LRE pass 4, then cold Chemical Detector pass 4. No session was chosen,
skipped or repeated for its outcome: SRanger pass 4 runs next, with its prompt as
committed, and the order continues from there (Reference round 2 after cold round
4). From now on `scripts/v3_start.sh` refuses any session but the next one in
the order above, computed from the committed sessions.

## Amendment 4 (30 October 2026, after cold SRanger pass 4, before any re-run)

**Trigger.** In cold SRanger pass 4, the first session set up under Amendment 3,
the runner stopped before the first phase: the pipeline's manifest loader
(`forge.dashboard/web/manifest.py`) reads
`docs/superpowers/specs/pipeline-manifest.schema.json`, which Amendment 3's
removal of `docs/` deleted. The check behind Amendment 3's statement that no
pipeline step reads the removed files searched for the joined path and missed
this one, which the loader builds from separate parts. The record shows 0 passed,
0 failed and 12 phases without a verdict. The session wrote its Java, trace and
review, ran the runner once and stopped; it opened no report.

**Classification.** A setup fault, not a verdict on the code: no phase ran. Under
Scoring above, the iteration is re-run.

**The change to setup.** The schema file is kept (it names no study; it is the
only file under `docs/` outside `docs/corpus/` that any pipeline step reads, by a
search of the pipeline's Python, Java, YAML and shell sources for the token
`docs` as well as for paths). Setup's verification also now loads the pipeline
manifest in the worktree with the runner's interpreter and refuses the worktree
if loading fails, so a missing file the pipeline needs stops setup instead of a
session.

**The re-run of SRanger pass 4.** The operator, not the actor: (1) confirm that
the worktree's Java and trace are byte-identical to the staged record and that
nothing else in the worktree changed after the runner; (2) move the faulted
staged record aside to `iter-1-setup-fault/` (kept, and committed with the run);
(3) restore the schema file in the worktree from the pin; (4) run the runner
command the prompt gives, once. Its result is the pass's verdict. The session is
not resumed and receives no message. No other session is affected: the fourteen
before it were set up before Amendment 3.

## Amendment 5 (30 October 2026, after cold Chemical Detector pass 5, before its verdict)

**Trigger.** Three sessions read `pipeline.yaml` beyond the prompt's preflight
line: Reference LRE run 1 (`grep -n -A5 'agent' pipeline.yaml | head -30`), cold
LRE pass 5 (`cat pipeline.yaml | head -40`) and cold Chemical Detector pass 5
(`grep -n -A3 'agent' pipeline.yaml | head -20`). The first two were declared void
on 30 October as forbidden reads, since `pipeline.yaml` is not on the reading
list above. The third read led to the finding that the reading list contradicts
the instructions on it: the study-free `CLAUDE.md` (line 14) and layer prompt
`01_architecture.md` (lines 8–9), both on the reading list, tell the session that
`pipeline.yaml`, key `agent.active_case_study`, names the case study. All three
sessions read the file while looking up their study's name, as those instructions
direct.

**The change.** Reading `pipeline.yaml` is a permitted read. Writing it remains
an instrument change and voids a session, as before. After Amendment 3 the file
names no study but the session's own (`active_case_study`); at the first pin it
carried two comment lines naming studies, neither in the lines Reference LRE run
1 read (checked against `5280e26e`).

**Effect.** The two void declarations are withdrawn: cold LRE pass 5 and
Reference LRE run 1 are valid, with their records unchanged. Their replacements
(cold LRE pass 11, Reference LRE run 6) are withdrawn unused; their prompts stay
in the repository, marked withdrawn. The void declarations, their withdrawal and
this amendment are reported. No other rule changes.

## Amendment 6 (30 October 2026, after Reference LRE run 3, before the pipeline change)

**Trigger.** Iteration 1 of Reference LRE run 3 failed Dafny verification with
`unresolved identifier: MAX_VALUE`: the Java constant
`static final int NO_INDEX = Integer.MAX_VALUE;` became
`const NO_INDEX: int := MAX_VALUE`. The Dafny generator renders the finite
`Double.MAX_VALUE` and `Double.MIN_VALUE` as their decimals (the constant fix of
28 October), but the same constants of the other numeric wrapper types fall
through to the bare field name. No written rule forbids them. (The iteration also
failed the profile check on a missing `@RoboChartType("real")`, a fault in the
code, so the run's verdict, converged at iteration 2, does not depend on this
change.)

**The change.** In `java2dafny.egl`, `MAX_VALUE` and `MIN_VALUE` of `Integer`,
`Long`, `Short` and `Byte` are rendered as their exact integer values (negative
ones in parentheses), and those of `Float` as the decimal Java prints for them,
as for `Double`. Infinities and NaN stay unsupported. Nothing else changes.

**Regression and pin.** As for Amendment 2: a test, then every reported Java
source extracted with and without the change; the only permitted differences are
Dafny lines that replace a bare `MAX_VALUE`/`MIN_VALUE` by its value. The change
is committed as a new pin, and every session from the next one on is set up at
it. Sessions already collected keep their verdicts unless the regression changes
one of their artefacts; any such session is reported with both verdicts.

**Done (30 October).** The change and its test are research commit
`d526ea5e3c0563f4019238d6ff1ee32cdc10f5f8`, the new pin; against `2db4c031` its
instrument differs only in them. The regression (324 sources, in
`experiments/regression-2026-10-30-wrapper-constants/`) changes one artefact
line: iteration 1 of Reference LRE run 3, `const NO_INDEX: int := MAX_VALUE`
becomes `const NO_INDEX: int := 2147483647`. That iteration still fails the
profile check, so the run's verdict (converged at iteration 2) is unchanged. The
prompts change only in the `FORGE_SHA` value; the 26 sessions already collected
used the prompts as committed before this amendment.

## Amendment 7 (31 October 2026, after cold LRE pass 8, before any change)

**Trigger.** In cold LRE pass 8 the VS Code Java extension injected a diagnostic
naming the project `chem-detector-java.generated.project`. The Gradle scaffold
every session starts from names the Chemical Detector: `java.generated.project/
settings.gradle` has `rootProject.name = 'chem-detector'` and `build.gradle` has
`group = 'chemdetector'`. Every session reads these files (the layer prompts direct
it to the scaffold). The leakage gate did not scan the scaffold. A scan of every
other file a session may read, including `pipeline.yaml`, finds no other study
name.

**Assessment.** The two strings carry no design content, and no SRanger or LRE
session's Java mentions them. They are nevertheless another study's name in an
input of every SRanger and LRE session, which the no-indirect-leakage rule of this
pre-registration excludes. Chemical Detector sessions saw only their own study's
name.

**The change.** The scaffold's names become neutral (`rootProject.name =
'controller'`, `group = 'controller'`); nothing in the pipeline reads them. The
leakage gate also scans the scaffold's non-source files and `pipeline.yaml`'s
non-comment lines. The change is a new pin. Worktrees and staging folders of
sessions set up from now on use new names (`forge-v3c7`, `forge-v3r7`,
`v3c7-staging`, `v3r7-staging`), so that each session's Claude Code log stays
separate from the superseded session of the same number.

**Effect.** The SRanger and LRE sessions collected so far (cold passes 1–8 of each,
Reference runs 1–3 of each; 22 sessions) are superseded: their records are kept,
moved to `experiments/v3-superseded-a7/`, and reported in the response letter, and
both studies are re-collected from pass 1 and run 1 at the new pin, in the order
above. The Chemical Detector sessions (cold passes 1–7, Reference runs 1–3) stand.
In the re-collection the order check skips sessions already collected, so the
Chemical Detector's remaining sessions follow their place in the order; this
deviation from the original interleaving is reported. Analyses report the pin
of every session.

**Done (31 October).** The scaffold change and the gate extension are research
commit `56cb43d7835b43e8a986e6ba4c0e82f35083d6f1`, the new pin; against `d526ea5e`
its instrument differs only in the two scaffold lines. The gate flags both lines
at the old pin and none at the new one; its test suite has 25 cases, all passing.
The prompts change only in the pin, the worktree prefix and the staging folder.

## Amendment 8 (31 October 2026, after cold SRanger pass 2 of the re-collection, before any change)

**Trigger.** Cold SRanger pass 2 received an IDE notice with the path of
`.gitignore`, which was open in the VS Code window. The session did not read the
file. `.gitignore` names two studies (the SRanger JUnit bundle and the LRE
reference-specification files) and was not scanned by the leakage gate. Checking
it showed a second gap: setup's removal of `pipeline.yaml` comments that name a
study (Amendment 3) required a non-letter on both sides of `lre`, so the comment
`LreController_Check discharges in ~71 s` stayed in every worktree; Amendment 7's
gate extension skipped `pipeline.yaml` comments on the assumption that setup had
removed them.

**Assessment.** No session in the collection read `.gitignore`, and no valid
session read `pipeline.yaml` beyond the preflight line, which reads only
`active_case_study`. No study content reached any session through either file, so
no session is superseded.

**The change.** Setup removes every line of the worktree's `.gitignore` and every
comment line of its `pipeline.yaml` that names a study, using one pattern that also
matches joined and prefixed forms (`LreController`, `chemdetector`). The leakage
gate scans `.gitignore` and all of `pipeline.yaml` except the session's own
`active_case_study` line, and setup fails if any line remains. No pin change: the
instrument is unchanged, and only setup's removals grow. The sessions collected at
the Amendment 7 pin before this amendment (cold SRanger passes 1 and 2, cold LRE
pass 1) stand and are reported with this difference.

**Done (31 October).** `scripts/v3_strip_study_names.sh` removes the four
`.gitignore` lines and the three `pipeline.yaml` comments (two of them already
removed by Amendment 3's pattern) and nothing else. The gate's LRE pattern also
matches underscored and prefixed forms (`LRE_`, `LreController`). At the pin the gate
flags those seven lines; after setup's removals it flags none, with each study
active. The test suite has 29 cases, all passing.

## Amendment 9 (1 November 2026, after the Steam Boiler Reference run, before any change)

**Trigger 1: a file naming another study.** `forge.assets/corrections/csp_overrides.csp`,
tracked at the pin since May 2026, opens with "CSP-M instantiation overrides for the
LRE controller" and lists LRE's safety constants and interpretations of LRE's
functions. Setup did not remove it and the leakage gate did not scan
`forge.assets/corrections/`. Two sessions read it: cold SRanger pass 1 (Read tool) and
Reference SRanger run 4 (`cat`). Four others saw the file name and did not open it.
The file is not part of the instrument: the CSP correction step
(`forge.dashboard/web/csp_corrections.py`) loads overrides from
`forge.dashboard/corrections/csp_overrides.csp`, which does not exist, and the CSP that
FDR4 checked in every recorded iteration carries the built-in ranges and the
generator's stubs, not this file's content.

**Trigger 2: an unrecorded run.** The recorder (`review/codex-prompts/snapshot_v2.py`)
accepts only `sranger`, `lre` and `chemical_detector`, so it refused all three
iterations of the Steam Boiler Reference run, and the runner printed its next-step
message regardless. The session stopped correctly at iteration 3 on a report directing
a `pipeline.yaml` change (FDR4 killed at the 10,240 MB memory limit). Only the final
iteration's reports and Java survive, and the runner's per-phase summary lines for
iterations 1 and 2; analysis item 5 needs every iteration's verdicts.

**Changes.** None to the instrument (the pin stays `56cb43d7`; `pipeline.yaml`,
including FDR4's `memory_limit_mb: 10240`, is unchanged).
1. Setup removes `forge.assets/corrections/csp_overrides.csp` from the worktree, as
   Amendment 3 removes unread files that name a study; the gate also scans every file
   in `forge.assets/corrections/` that setup leaves.
2. The recorder accepts `steam_boiler`.
3. The runner, when recording fails, prints `REFUSED: the iteration could not be
   recorded. Stop and report this line.` in place of the next-step message and exits
   non-zero; sessions see this text only on a recording failure.
4. The start script checks, before setup, that the recorder accepts the study.
5. New worktree prefixes and staging folders for the sessions collected after this
   amendment (`forge-v3c9`, `forge-v3r9`, `v3c9-staging`, `v3r9-staging`), because
   setup refuses a worktree that already holds Java; the prompts are regenerated with
   these names only.

**Superseded and re-collected.** Cold SRanger pass 1 and Reference SRanger run 4 (they
read the file) and the Steam Boiler Reference run (unrecorded). Their records move to
`experiments/v3-superseded-a9/`, the Steam Boiler run with the evidence preserved from
its worktree and scratchpad. They are re-collected in that order, after which the
collection is complete. Every other session stands: none opened the file.

**Disclosed, not changed.** The FDR4 verdicts of every session are at a `{0..1}`
instantiation of `nat`, `int`, `real` and the clock type (`forge.dashboard/corrections/
type_ranges.json` and the built-in defaults; the communicated-constant widening fired
in no session), and LRE's distance functions are the generator's stubs (`odist`,
`hdist`, `vdist` and `sqrt` return 0), which is why FDR4's LRE check covers the
operator-control mode only. An FDR4 check of the Steam Boiler's final Java at a larger
memory limit, if made, is exploratory and reported apart from the pre-registered
results.

## Amendment 10 (1 November 2026, after the re-collected Steam Boiler Reference run's iteration 1, before any change)

**Trigger.** The Steam Boiler Reference run re-collected under Amendment 9 stopped at
iteration 1: FDR4 was killed at the 10,240 MB memory limit (peak 10,308 MB; the
superseded run peaked at 10,311 MB), and its report directs the operator to raise
`phases.fdr4.memory_limit_mb` or record the assertions as unmeasured. The session
stopped and reported this, as the prompt requires. The authors decide to give FDR4
as much memory as the machine allows, so that the Steam Boiler's FDR4 verdict is
measured rather than cut off by the budget.

**Change to the instrument.** The FDR4 memory budget goes from 10,240 MB to 40,960 MB
(40 GiB of the machine's 48 GiB, leaving about 8 GiB for the operating system, the
editor and the agent). Nothing else changes; the FDR4 timeout stays 9,000 s.

*Mechanism (corrected 1 November 2026, before any change).* The budget is not taken
from the pinned `pipeline.yaml` (which carries 16,384 MB): setup
(`scripts/setup_claude_run.sh`, step B2) rewrites the worktree's
`phases.fdr4.timeout` and `phases.fdr4.memory_limit_mb` to the collection's
budget and verifies them, so every session has run at 9,000 s / 10,240 MB whatever
the pinned file says. The change is therefore made in that setup step and its check;
the pin stays `56cb43d7`. An earlier version of this paragraph said a new pin
commit would carry the change; no such commit was made.

**Effect on the sessions already collected: none.** The limit only stops a check
that exceeds it. In the 48 recorded iterations of the 45 sessions collected, FDR4
passed 46 times, failed once on a CSP parse error (Reference Chemical Detector run 4,
iteration 1), and did not run once (cold Chemical Detector pass 3, whose CSP
generation failed at the earlier pin); no iteration was killed for memory. A
completed FDR4 check gives the same verdict under any limit it stayed within, so
every collected verdict is the verdict the new instrument gives. These sessions
stand.

**Superseded and re-collected.** The Steam Boiler Reference run collected under
Amendment 9. Its record (iteration 1, the transcript and its audit) moves to
`experiments/v3-superseded-a10/`. The run is re-collected at the new pin with a new
worktree prefix and staging folder (`forge-v3r10`, `v3r10-staging`), because setup
refuses a worktree that already holds Java; its prompt is regenerated with the new
names only.

## Note on Amendment 10 (1 November 2026, after the Steam Boiler run converged; a disclosure, not a change)

The FDR4 memory budget is enforced on the resident set size of `refines` as `ps -o rss`
reports it (`forge.dashboard/web/runners.py`, `_get_process_memory_mb`). On macOS
that figure leaves out compressed pages. In iteration 3 of the Steam Boiler run the
check's footprint reached about 96 GiB (kernel message `killing largest compressed
process _refines [12750] 98418 MB`, 12:17:53) while its resident size stayed between
5 and 7 GiB; the kernel's memory-pressure killer, not the pipeline, ended it after
961 s, and the runner labelled the failure "CSP-M parse error". So:

- every budget in this collection, 10,240 MB before Amendment 10 and 40,960 MB after,
  is a budget on resident memory, applied the same way to every session; the two
  superseded Steam Boiler runs were stopped by it at 10,308 and 10,311 MB resident;
- after Amendment 10 the binding limit on FDR4 is the machine's memory, which is the
  amendment's intent;
- no collected verdict changes: no other iteration was killed for memory by either
  the pipeline or the system.

The mislabelling (a check killed from outside is reported as a parse error, because
the runner treats a run with no parsed result as a parse error before it looks at
the exit status) is a reporting defect of the instrument. It is disclosed here and
in the paper; the instrument is not changed.

## Scoring note (1 November 2026, after the collection, before the paper's tables; a decision on the wording of Scoring)

**FDR4 in iteration 3 of the Steam Boiler run.** Scoring says an environment
outcome, "an external kill, a host or Docker fault, a tool timeout from machine
load", is not a verdict and the iteration is re-run on a quiet machine. The kill in
iteration 3 came from inside the check: FDR4's own footprint (98,418 MB) exceeded
the machine's 48 GiB. The pipeline's budget of 40,960 MB set by Amendment 10 applies to
resident memory and never fired (see the note on Amendment 10), so the machine's
memory was the limit that bound, and a re-run on a quiet machine cannot finish either. The authors therefore count it as
recorded, "no verdict within the machine's memory" (class (e) in the analysis), not
as an external kill. The run stays converged at iteration 4. This reading of the
rule is reported in the paper.

**Isabelle in the same iteration.** Its deadlock-freedom proof timed out (tactic
timeout, 597 s) shortly after the kill. To tell a verdict at the pipeline's per-goal
timeout from a timeout caused by machine load, only that phase is re-run on the
recorded theory on a quiet machine with `scripts/v3_rerun_phase_isabelle.sh`
(Amendment 1's procedure). The result is reported beside the recorded verdict and
does not change the iteration count: iteration 3 fails on FDR4 either way.

## Amendment 11 (1 November 2026, after the collection and its analysis, before any new session)

**Why, and when.** The collection above is complete and analysed. The Steam Boiler
is the one study whose loop run needed more than one iteration, and it was run
once, with no cold pass. The authors decided, knowing that result, to collect more
Steam Boiler sessions, so that its convergence is measured over five runs as for the
other studies and a cold pass is observed. The decision is made after the results;
this amendment fixes the new sessions, their order and their analysis before any of
them runs.

**Added sessions.** At the same pin `56cb43d7`, with the same instrument, setup
(Amendments 3, 4, 7, 8, 9 and 10, FDR4 budget 9000 s / 40960 MB), prompts (generated
by `review/make_v3_prompts.py`, differing from existing prompts only in study and
number), validity and scoring rules (the scoring note's reading of a memory kill
included):

| Collection | Condition | Study | Sessions | Iterations |
|---|---|---|---|---|
| `v3-cold` | Cold: one single pass | Steam Boiler | pass 1 | 1 |
| `v3-reference` | Reference loop | Steam Boiler | runs 2, 3, 4, 5 | up to 7 |

**Order.** After the existing Steam Boiler run: cold Steam Boiler pass 1, then
Reference Steam Boiler runs 2, 3, 4 and 5, one at a time. A void session is replaced
by the next number of its collection (cold pass 2; Reference run 6), after the order
above.

**Analysis (extends item 5).** For the five Reference runs: converged count,
iterations to convergence (median and range; a run at the cap is not converged),
per-phase verdicts per iteration, and every failure classified as in item 1. For the
cold pass: its verdict and, if it fails, the first failing phase and its class. The
Steam Boiler's first passes (the cold pass and the five runs' first iterations) are
reported together, as a count, with no test. Best-of-N is not computed for the Steam
Boiler (one cold pass). Nothing is pooled with Tables 5 to 7: the counts for the three
original studies are unchanged, and the Steam Boiler is reported in its own section.

**Reporting.** As above: every session is reported whatever it shows, and none is
dropped or re-run for its outcome. Run 1's record is unchanged.

## Amendment 12 (1 November 2026, after Amendment 11 and setup of cold Steam Boiler pass 1, before any new session's verdict)

**Change.** Amendment 11's single cold Steam Boiler pass becomes ten, so the Steam
Boiler gets the same comparison as the other studies. Pass 1 is the pass Amendment 11
set up; its prompt and setup are unchanged.

| Collection | Condition | Study | Sessions | Iterations |
|---|---|---|---|---|
| `v3-cold` | Cold: one single pass | Steam Boiler | passes 1 to 10 | 1 |
| `v3-reference` | Reference loop | Steam Boiler | runs 2 to 5 | up to 7 |

**Order.** Interleaved as in the main collection: cold Steam Boiler k for k = 1..10,
with Reference Steam Boiler run j + 1 after cold pass 2j (j = 1..4). A void session
is replaced by the next number of its collection (cold 11, 12, ...; Reference 6, 7,
...), after the order above.

**Analysis (replaces Amendment 11's).** For the Steam Boiler alone, with the methods of
items 1 to 4: the cold converged count, each failure classified (item 1); the five
Reference runs as in item 2; best-of-N for N = 1, 2, 3, 5, 10 under both selectors, the
matching budget the median iterations of the five Steam Boiler runs rounded up, each N
against the Steam Boiler Reference converged count by a one-sided Fisher exact test
(item 3); and the same-condition check (item 4). Also, side by side and with no test,
the Steam Boiler's cold converged rate beside each of the three original studies'.
Nothing is pooled with Tables 5 to 7, whose counts are unchanged; the Steam Boiler is
reported in its own section.

**Reporting.** As above, whatever the result shows, including a result in which
sampling matches or beats the loop.

## Amendment 13 (1 November 2026, after Steam Boiler Reference run 2 stopped at the cap, before any change)

**Why, and when.** Steam Boiler Reference run 2 did not converge within the cap of 7
iterations; at iteration 7, FDR4 and Isabelle still fail. The authors decided, after
seeing that result, to remove the cap for the Steam Boiler Reference runs, so that the
study's loop is measured until it converges. None of the 15 Reference runs of the three
original studies and neither earlier Steam Boiler run used more than 4 iterations, so the
cap bound no other session and no reported count changes.

**Change.** For Steam Boiler Reference runs 2 to 5 (and replacements), there is no
iteration cap. A run stops when:

1. all twelve phases pass (it converged at that iteration); or
2. the session concludes that the remaining failures cannot be repaired in its Java
   without changing the pipeline, and says so: it is recorded as not converged at that
   iteration, with the failing phases and the session's stated reason.

**Run 2.** Its pre-registered result stands and is reported as such: not converged at the
cap of 7. The same session then continues, on one fixed operator message (below), from
iteration 8 under the stopping rule above; its iterations 8 onward are recorded in
`run-2/continuation-a13/` and reported separately, as a continuation past the cap. The
continuation message is the only operator input permitted after the first; it is not a
deviation. Text, pasted verbatim:

> Pre-registration Amendment 13 removed the iteration cap for this run. Continue the loop
> from iteration 8 under the same rules as before. Stop when the runner prints ALL PHASES
> PASSED, or when you conclude that the remaining failures cannot be repaired in your Java
> without changing the pipeline; in that case stop and report non-convergence with the
> failing phases and your reason.

**Runs 3 to 5.** Their prompts replace the loop's step 5 (the cap) with the stopping rule
above; nothing else in them changes.

**Analysis.** Item 2 and Amendment 12 for the Steam Boiler use each run's iterations to
its stop; run 2 is reported twice, at the cap (pre-registered) and with its continuation.
Best-of-N's matching budget (Amendment 12) uses the median iterations of the five runs,
run 2 counted at its continuation's stop, or as 7 if the continuation also stops without
converging.

## Amendment 14 (2 November 2026, after Steam Boiler Reference run 2, before any pipeline change)

**Trigger.** Cold Steam Boiler passes 1 and 2 and Steam Boiler Reference run 2 met
defects of the pipeline on Java that follows the written rules; run 2's session named
three of them. Checked against the generated files:

1. **Isabelle: guard functions mistyped.** The Isabelle generator does not read the type
   the RoboChart model declares for a sensor query (`var levelAboveNormalBand : boolean`
   in the `Sensors` interface). It guesses: `bool` only if the call appears directly
   under `¬` somewhere, otherwise `real`. A boolean query used only positively is
   declared `unit ⇒ real` and the theory fails to type-check (or, when the clash is in an
   operation's precondition, Isabelle aborts with `exception Empty`). The rule predicts
   138 of 140 boolean guard functions in run 2's 14 theories.
2. **Dafny: brackets dropped.** The Dafny printer prints Spoon's operator tree without
   restoring brackets, in guards (`a && (b || c)` becomes `a && b || c`, which Dafny
   rejects) and in arithmetic (`x * (y + z)` would become `x * y + z`).
3. **Output-event value dropped.** An output event whose value is a sensor call
   (`OpenPump(sensor.fillPump())`) is extracted as `send openPump`, without the value; the
   CSP is ill-typed when another branch sends the same event with a literal.
4. **FDR4's memory budget does not fire on macOS.** The monitor measures resident
   memory (`ps -o rss`), which leaves out what macOS compresses; FDR4 grew to 79-98 GB
   and was ended by the system.
5. **A killed FDR4 is reported as a "CSP-M parse error",** with a repair directive about
   templates.

Run 2's third named defect (a predicate that is a bare sensor call keeps the predicate's
name in the RoboChart model and the Isabelle theory, while Dafny uses the method's name)
is a documented design choice of the extractor; all three chains treat the value as an
unconstrained boolean input. It is not changed.

**Changes.**

1. `thy_generation_rule.egl`: a guard function that the model declares as a variable of
   the `Sensors` interface takes the declared type; the usage guess applies only to names
   the model does not declare.
2. `java2dafny.egl`: an operand gets brackets when Java's precedence requires them (a
   lower-precedence child; an equal-precedence right child of `-`, `/` or `%`; a binary
   operand of a unary operator), and `&&` and `||` operands of each other are always
   bracketed, as Dafny requires. Where no bracket is needed the output is unchanged.
3. `java2robochart.etl`: an output-event constructor argument that is neither a literal
   nor a named constant is translated as an expression (a sensor call becomes the
   model's call of that function), instead of being dropped.
4. `runners.py`: on macOS the memory monitor reads the process's physical footprint
   (resident plus compressed memory, as macOS's own memory accounting does) instead of
   resident memory. The budget stays as set under Amendment 10.
5. `runners.py`: an FDR4 process ended by a signal with no verdict is reported as killed
   (`fdr4_signal_kill`), not as a parse error. The memory-kill directive no longer tells
   the actor to raise the budget in `pipeline.yaml` (Amendment 10 already set it to the
   machine's limit); it says the state space exceeded the machine's memory, that the
   budget cannot be raised, and that the actor may reduce the controller's state space
   only without changing its required behaviour.

**Regression and pin.** As for Amendments 2 and 6: tests for each change, then every
reported Java source and every collected iteration extracted with and without the
change. Permitted differences: (1) a guard function's declared Isabelle type changes to
the type the model declares; (2) Dafny lines that differ only by brackets; (3) a send
that gains a value. Changes 4 and 5 do not touch extraction and are stub-tested. The
change is committed as a new pin; every Steam Boiler session from the next one on is set
up at it. The four Steam Boiler sessions already collected (cold passes 1 and 2,
Reference runs 1 and 2) keep their verdicts and are re-scored at the new pin through all
twelve phases, reported beside the original verdicts. Any other collected session whose
artefacts change is re-scored the same way and reported with both verdicts.

## Note on Amendment 14 (2 November 2026, after its regression, before the pin)

The regression over 390 Java sources (every reported source and every collected
iteration) found two kinds of difference outside the permitted ones, both in mutants of
the mutation campaign, and both from changes broader than intended:

- Change 1 also applied to names the extractor could not resolve to a Java method, which
  it declares `nat` as a marker (e.g. a payload variable leaked by a trigger-swap
  mutant), replacing the usage guess with that marker (`Angle` became `nat`, 18 cases).
  Change 1 now uses the declared type except `nat`.
- Change 3 also translated a value that reads a local variable: in two LRE trigger-swap
  mutants, the payload read `rv.value()` outside its trigger's branch became a spurious
  sensor variable `rv`. Change 3 now translates only values that read no local variable;
  such a value is dropped as before.

Both refinements only narrow the changes. The regression is run again in full, and the
pin is committed only if every difference is of a permitted kind.

## Amendment 15 (2 November 2026, after Amendment 14, before any change and before any new session)

**Decision.** Every Steam Boiler session collected before the Amendment 14 pin is
superseded: cold passes 1 and 2 and Reference runs 1 and 2. All four ran at a pin with the
five defects Amendment 14 fixes, and three of them met those defects (cold passes 1 and 2
failed on them; Reference run 2 spent most of its iterations on them). The rule is the
pin a session ran at, not its verdict: run 1, which converged, is superseded with the
others. Reference run 2 is not audited further; its records are kept as collected.

**Records.** The four sessions move to `experiments/v3-superseded-a15/`, with their
transcripts, audits (where written) and run 2's unaudited iterations 1-14 from staging.
Each keeps its recorded verdict. The paper reports them as a superseded collection with
their verdicts and the reason, and counts none of them. Amendment 14's re-scoring of the
four sessions at the new pin is withdrawn: they are re-collected instead.

**Re-collection.** The Steam Boiler is collected again from the start at the Amendment 14
pin: ten cold passes and five Reference runs, interleaved as cold 1, cold 2, Reference 1,
cold 3, cold 4, Reference 2, ..., cold 9, cold 10, Reference 5. No Reference run has an
iteration cap; each stops on convergence or when the session reports that the remaining
failures cannot be repaired in its Java without changing the pipeline (Amendment 13's
rule, now for all five). New worktree and staging names (`forge-v3c15`, `forge-v3r15`,
`v3c15-staging`, `v3r15-staging`). The prompts are the Amendment 13 prompts with the new
names; nothing else changes. Void replacements follow the existing rule (cold 11, 12,
...; Reference 6, 7, ...). The analysis of Amendments 11 and 12 applies to the new set
unchanged.

## Amendment 16 (2 November 2026, after cold Steam Boiler pass 1 of the Amendment 15 re-collection, before any change)

**Trigger.** Cold Steam Boiler pass 1, at the Amendment 14 pin `d714365c`, met two more
defects of the pipeline on Java that follows the written rules:

1. **FDR4: literal values outside the checked range.** The Java sends the pump numbers as
   literals (`new OutputEvent.OpenPump(1)` ... `(4)`); the model sends `openPump ! 1` ...
   `openPump ! 4` on an event typed `int`. The checked instantiation gives every numeric
   type the range {0, 1}, the agreed bound that keeps FDR4's state space tractable. The
   range step widens a type for named constants the model sends, not for literal values,
   so FDR4 rejects `openPump.out.2` ("not a member of the set {0, 1}") and returns each
   assertion with only that error; the runner reports them as "inconclusive".
2. **Isabelle: a record field named after a keyword.** The Java record
   `PumpReading(int pump, boolean open)` becomes an Isabelle record with a field `open`,
   an outer-syntax keyword of Isabelle/Pure; the theory does not parse. The generator
   renames only `value`, `values`, `term` and `prop`.

**Changes (one commit; the new pin).**

1. **Per-channel value sets.** The range step keeps every numeric type at {0, 1}: all
   state variables, constants and other channels are checked exactly as before. For each
   event typed `int` or `nat` on which the model sends an integer literal outside {0, 1},
   that event's channel alone (its declarations `channel e: InOut.core_int` and
   `channel e__: NIDS.InOut.core_int` in every generated CSP file, untimed and timed)
   gets the value set `{lo..hi}` spanning {0, 1} and those literals. Each such change is
   logged. Named constants keep the existing rule unchanged.
2. **Isabelle record fields.** A record field whose name is a keyword of Isabelle/Pure or
   Isabelle/HOL (the `keywords` clauses of `Pure.thy` and `HOL.thy` in the pinned
   distribution) gets a trailing underscore, as `value` already does. No theory accesses
   record fields by name, so the declaration is the only place the name appears.

**Regression.** Every source of Amendment 14's regression is extracted with and without
change 2. Permitted difference: a record field renamed with a trailing underscore. Change 1
acts in the FDR4 phase; its reach is measured on the same extractions as the sources whose
model sends an integer literal outside {0, 1} on an `int` or `nat` event. A converged
iteration cannot be among them (its FDR4 check passed, which such a literal prevents);
every committed session iteration or mutant that is among them is re-scored at the new pin
and reported with both verdicts.

**The Steam Boiler.** By the rule of Amendment 15 (the pin a session ran at), cold Steam
Boiler pass 1 of the Amendment 15 re-collection is superseded (kept in
`experiments/v3-superseded-a16/`, not counted) and the Steam Boiler is collected from the
start at the new pin, in Amendment 15's order, with new worktree and staging names
(`forge-v3c16`, `forge-v3r16`, `v3c16-staging`, `v3r16-staging`). The prompts are
Amendment 15's with the new names and pin; nothing else changes.

## Amendment 17 (2 November 2026, after Steam Boiler Reference run 1 of the Amendment 16 re-collection, before any change)

**Trigger.** Every FDR4 check of the three Steam Boiler sessions collected at the
Amendment 16 pin (cold passes 1 and 2; Reference run 1, iterations 1-3) was stopped at
the 40,960 MB budget of Amendment 10 with no verdict, on models of 30-43 transitions.
Reference run 1 converged at iteration 4 only after moving the valve and pump commands out
of the verified controller (20 transitions). The operator has freed disk space (232 GiB
free) so FDR4 can page to disk.

**Change (FDR4's budget only; the pin and the pipeline are unchanged).** For Steam Boiler
sessions setup applies, as it applies the budget under Amendment 10:

1. `phases.fdr4.memory_limit_mb: 81920` (80 GiB of the physical footprint the monitor
   reads, Amendment 14); `timeout` stays 9000 s.
2. FDR4 runs through `scripts/fdr4_paged.sh` (set as `phases.fdr4.fdr4_path`), which
   empties a store directory and then replaces itself with `refines` (so the monitor and
   the runner see FDR4's own process), adding FDR4's disk store:
   `--refinement-storage-file-path /Users/ranwei/Gitee/fdr4-store`,
   `--refinement-explorer-storage-type LSMTree` and
   `--refinement-storage-file-cache-size 50%`. Explored states beyond the in-memory cache
   go to disk; the checked model, its assertions and its value ranges are unchanged.

The three other studies are complete; none of their FDR4 checks was stopped for memory,
so the change could not alter any of their verdicts.

**The three collected sessions (operator's decision: keep and re-score).** They stay
counted with their recorded verdicts. Every iteration whose FDR4 check was stopped at the
budget (cold pass 1, cold pass 2, Reference run 1 iterations 1-3) is re-scored once at the
same pin with the new FDR4 settings, from its recorded Java, through all twelve phases;
both verdicts are reported. Reference run 1 is reported as recorded (converged at
iteration 4), with the disclosure that its later iterations responded to FDR4 feedback
under the earlier budget, which a re-score cannot recreate. The re-scores run before the
next session (cold pass 3), one at a time, and serve as the check that the new settings
work; if they do not, the settings are revisited by a further amendment before any
session.

## Note on Amendment 17 (2 November 2026, after the first re-score attempt, before any session)

The first re-score (cold pass 2, iteration 1) did not run FDR4: `refines` rejected the
option `--refinement-storage-file-cache-size 50%` ("Too little memory specified for the
in-memory cache. The minimum is 3.75GB."), and the runner reported the refusal as a CSP-M
parse error. The option takes an absolute size; the wrapper now passes `24GB`, half of the
48 GiB machine, the cache Amendment 17 intended. Nothing else changes. The attempt's output
is kept as `rescore-a17-attempt1-option-rejected/` and carries no verdict on FDR4; the
re-score is run again.

## Second note on Amendment 17 (2 November 2026, after the first completed re-score, before any session)

The re-score of cold pass 2, iteration 1, ran FDR4 with the Amendment 17 settings. macOS's
memory-pressure killer ended it at 80,743 MB (system log, 19:03:27 UTC) during compilation
of the model ("Found 18000 processes including 87 names"; no assertion result); FDR4's
disk store stayed empty. The settings stay as Amendment 17 states for the remaining
sessions (the most memory FDR4 can be given on this machine); a kill by the operating
system is classed (e) like a stop by the monitor. The remaining four re-scores are run as
pre-registered.

## Third note on Amendment 17 (2 November 2026, after the five re-scores, before cold pass 3)

All five iterations stopped at the 40,960 MB budget were re-scored with Amendment 17's
settings (cold pass 1; cold pass 2; Reference run 1, iterations 1-3). None reached an FDR4
verdict: two were stopped by the pipeline's monitor at the 81,920 MB budget (peaks 81,940
and 81,994 MB) and three were ended by macOS's memory-pressure killer at 80,743, 81,165
and 76,745 MB (system log). FDR4's disk store stayed empty in all five, and in the one run
whose progress output was kept FDR4 was still compiling the model. Every other phase gave
the recorded verdict, so no counted verdict changes. Records: `rescore-a17/` beside each
run in the research repository.

## Scoring note (3 November 2026, during the Steam Boiler re-collection)

**Profile-check errors on `double` parameters.** Rule 4 of `java_codegen_rules.txt` says to
apply `@RoboChartType` on field declarations, method parameters and method return types,
and states that it is required ("the profile checker reports a missing one as an error")
on every `double` field. The profile checker also rejects `double` parameters without it.
A failing profile check whose only rule-4 errors are on parameters is therefore classed
(c), a pipeline defect on Java within the instructions (the checker enforces more than the
rule states), as the Chemical Detector Reference run 2 checker case is; one with any such
error on a field stays (a). Decided by the authors after cold Steam Boiler pass 5, before
any further session; it changes the class of three failing phases (cold Steam Boiler
passes 4 and 5, Reference Steam Boiler run 2 iteration 1) and no convergence verdict.

## Reporting

Every table is reported whatever it shows, including a result in which sampling
matches or beats the loop. No pass or run is dropped, re-rolled or re-run for its
outcome. Nothing from cold-baseline-v2 or any earlier collection is pooled with
these collections; the paper reports only these, and the letter reports the
earlier ones as results under the previous instructions.

## Stop rules

None on outcome. The collections stop when every pass and run above is collected
and audited.
