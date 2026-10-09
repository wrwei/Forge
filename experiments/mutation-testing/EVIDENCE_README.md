# Mutation-testing experiment — full evidence tree (RQ6)

Packaged 2026-08-31 from `/tmp/campaign`. This is the complete
input+output set behind Section 4.8 (RQ6) of the TOSEM revision, including every
mutant's generated artefacts and every verifier log. `/tmp` does not survive a
reboot; this tarball is the durable copy.

## Provenance — which extractor produced this

`prefix-snapshot/forge.transformations/` is the **pre-fix extractor** used for the
whole experiment: the committed state (26fe49fb), staged read-only, WITHOUT the
uncommitted T2M/M2M repairs made in this revision cycle. That is deliberate — RQ6
measures the extractor the paper reports. The separate supplementary arm
(`supplementary_fixed_extractor.csv`, its own artifact) re-ran the
extraction-invisible mutants through the fixed working tree.

## Layout

    mutants/<mutant_id>/java/...     mutated Java sources (186 mutants)
    outputs/<mutant_id>/             generated artefacts per mutant:
                                       LreController.dfy etc.   Dafny program
                                       robochart_controller.rct RoboChart model
                                       robochart_model.xmi      M2M output
                                       discovered_model.xmi     T2M output
                                       csp-gen/                 CSP (defs/, timed/)
                                       isabelle/                theory + ROOT
                                       trace_*.json             per-phase traces
                                       constant_defaults.json
    baseline/{lre,sranger,chemical_detector}/   same, unmutated
    fdr4-arm/                        FDR4 logs + framed_json verdicts
      rev1-broken/                   first run: defs-only staging broke the
                                     ../instantiations.csp include (0 verdicts)
      rev2-uncorrected/              second run: staging fixed but type ranges
                                     uncorrected (LRE state explosion, chem
                                     polymorphic-channel errors)
    isabelle-arm/                    Isabelle build logs, 86 changed theories
      docker-down/                   aborted run: Docker daemon not running
    isabelle-timeouts/               isolation diagnostic (2 variants x 11)
    rct_changed.txt                  134 mutants whose .rct differs from baseline
    thy_changed.txt                  86 mutants whose .thy differs from baseline
    driver/, classes/, *.txt         build scaffolding (classpath, jar lists)

The `rev*-broken` / `docker-down` directories are kept on purpose: three of the
four harness defects found in this experiment were silent (exit 0, zero verdicts),
and these logs are the evidence for the baseline sanity gates now in both arm
scripts.

## Mutant population

186 mutants, all compiling, generated deterministically (seed 20260815).

By study: chemical_detector 90, lre 71, sranger 25
By class: branch_swap 56, trigger_swap 41, off_by_one 25, relop_flip 23, cmp_swap 23, sentinel_init 9, drop_negation 9

## Results summary (full table: kill_table_all_backends.csv)

Extraction-visible: 142 of 186; invisible 44.

    Dafny    killed 13
    FDR4     killed 18  (3 assertion, 15 load-rejection)
    Isabelle killed 1  (definitional failure; 11 inconclusive, NOT counted)
    union    31

No mutant is killed by both Dafny and FDR4. The single Isabelle kill coincides
with an FDR4 load rejection on the same mutant.

## Reproducing / re-materializing

The arm scripts read and write `/tmp/campaign`, the working directory they ran
in (the per-mutant logs record that path). Copy this folder there first, from
the repository root:

    mkdir -p /tmp/campaign && cp -R experiments/mutation-testing/. /tmp/campaign

Then either arm script re-runs from there (both are resumable and both abort if
their baselines do not produce verdicts):

    bash scripts/fdr4_arm.sh
    bash scripts/isabelle_arm.sh
    bash scripts/isabelle_timeouts.sh

FDR4 needs a valid licence in the invoking user's context; Isabelle needs Docker
Desktop running (the CyPhyAssure distribution is Linux-x86_64 and runs
containerised via scripts/isabelle-docker.sh).
