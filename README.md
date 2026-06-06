# FORGE — Formal-Method-Guided Vibe Coding

FORGE turns LLM-written Java for safety controllers into formally verified
software. Java source is written interactively with LLM assistance, then
transformed through a formal-verification toolchain and checked by three
independent verifiers (FDR4, Dafny, Isabelle/UTP).

```
Java source (what you write/refine)
  → [Spoon]            EMF model (.xmi)
  → [ETL]              RoboChart model (.xmi)
  → [EGL]              RoboChart textual notation (.rct)
  → [RoboChart CSP gen] CSP-M           → [FDR4]      deadlock/divergence freedom
  → [Dafny gen]         Dafny           → [Dafny]     design-by-contract
  → [EGL thy gen]       Z-Machine .thy  → [Isabelle]  deadlock-freedom + invariants
```

The canonical phase manifest is [`pipeline.yaml`](pipeline.yaml) (12 phases:
compile, coverage, preflight, t2m, m2m, m2t, dafny_gen, isabelle_gen, fdr4,
dafny_verify, isabelle_verify, vacuity). Codegen rules, M2M naming conventions,
and per-phase commands live in [`CLAUDE.md`](CLAUDE.md).

## Repository layout

| Path | What it is |
| ---- | ---------- |
| [`CLAUDE.md`](CLAUDE.md) | Project instructions: Java codegen constraints, M2M/ETL conventions, per-phase commands, known gotchas |
| [`pipeline.yaml`](pipeline.yaml) | Phase manifest — runners, dependencies, outputs, and **tool configuration** (see below) |
| [`forge.assets/`](forge.assets/) | Case studies (`case-studies/<study>/` requirements + system descriptions), codegen prompts, correction feedback |
| [`forge.transformations/`](forge.transformations/) | Spoon discovery + ETL/EGL transformations + Dafny/Isabelle generation; vendored RoboChart CSP-gen jars in `lib/` |
| [`forge.dashboard/`](forge.dashboard/) | Web dashboard + the Python pipeline bridge the experiment scripts drive |
| [`java.generated.project/`](java.generated.project/) | Gradle workspace where the generated Java is written and compiled |
| [`experiments/`](experiments/) | Convergence + cold-baseline experiment data, run guides, and reproducibility scripts |

## Prerequisites

| Tool | Needed for | Notes |
| ---- | ---------- | ----- |
| **JDK 17** | compile, t2m, m2m, m2t, dafny_gen, isabelle_gen | Gradle itself is bundled via the `gradlew`/`gradlew.bat` wrappers — only a JDK 17 on `PATH` is required |
| **Python 3.11+** | the pipeline bridge + experiment scripts | runs `forge.dashboard/web` and `experiments/scripts/*` |
| **Dafny** | `dafny_verify` | design-by-contract verifier |
| **FDR4** | `fdr4` | tock-CSP refinement checker (`refines.exe`) |
| **Isabelle/UTP** (CyPhyAssure distribution) | `isabelle_verify` | Z-Machine prover; **Linux-only** — on Windows it runs inside WSL (see below) |
| **WSL2 + Ubuntu** | `isabelle_verify` on Windows only | hosts the Linux Isabelle install |
| **Claude Code CLI** (optional) | the dashboard chat panel only | not required to run the pipeline or the experiment scripts |

## Tool configuration

All external-tool locations are configured in [`pipeline.yaml`](pipeline.yaml).
Each `*_path` key is a **per-OS map** (`darwin` / `linux` / `win32`): a bare
binary name resolves via `PATH` (`shutil.which`), while an absolute path pins a
specific install. Set the entry for your platform.

| Setting (`pipeline.yaml`) | Purpose | Committed default |
| ------------------------- | ------- | ----------------- |
| `phases.dafny_verify.dafny_path` | Dafny executable | `darwin: dafny` · `win32: C:\Users\willr\dafny\dafny\Dafny.exe` |
| `phases.fdr4.fdr4_path` | FDR4 `refines` executable | `darwin: refines` · `win32: C:\Program Files\fdr\bin\refines.exe` |
| `phases.isabelle_verify.isabelle_path` | Isabelle launcher | `linux/darwin: isabelle` · `win32: wsl.exe` |
| `phases.isabelle_verify.wsl_distro` | WSL distro to target (Windows) | `Ubuntu` |
| `phases.isabelle_verify.wsl_isabelle_bin` | Isabelle binary **inside** WSL (Windows) | `/home/willr/isabelle/Isabelle2023-CyPhyAssure/bin/isabelle` |
| `agent.claude_path` | Claude Code CLI for the dashboard chat | `darwin/linux: claude` · `win32: claude.exe` |
| `agent.active_case_study` | which study under `forge.assets/case-studies/` is active | `chemical_detector` |

> ⚠️ **The committed defaults are the original author's machine paths**
> (`C:\Users\willr\...`, `/home/willr/...`). **Re-point `dafny_path` and
> `wsl_isabelle_bin` (and `fdr4_path` if FDR4 isn't on `PATH`) to your own
> installs before running.** These are local edits — don't commit them.

Two more knobs:

- **FDR4 type ranges** are pinned to `{0..1}` in
  [`forge.dashboard/corrections/type_ranges.json`](forge.dashboard/corrections/type_ranges.json).
  Wider ranges make the CSP polymorphic (won't compile) and can exhaust the
  Windows page file — keep them at `{0..1}`.
- **Phase timeouts / memory** are per-phase in `pipeline.yaml`
  (`fdr4.timeout` / `memory_limit_mb`; `isabelle_verify.timeout` /
  `proof_timeout` / `memory_limit_mb`). Set `isabelle_verify.memory_limit_mb`
  to your machine's page-file size.

### Windows / WSL (Isabelle only)

Isabelle/UTP is Linux-only, so on Windows the `isabelle_verify` phase bridges to
WSL via `wsl.exe`. One-time setup:

1. Install WSL2 + Ubuntu, then make it the default: `wsl --set-default Ubuntu`
   (Docker Desktop can hijack the default distro after a reboot — re-run if
   `wsl <cmd>` lands in `docker-desktop`).
2. Install Isabelle2023 with the CyPhyAssure distribution inside that distro and
   point `wsl_isabelle_bin` at its `bin/isabelle`.
3. Run only **one** Isabelle build at a time — concurrent `wsl.exe` invocations
   can deadlock the VM.

Phases 2a–6b and the vacuity audit run natively on Windows; only Isabelle proof
checking needs WSL. See
[`experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`](experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md)
§0 for the full WSL setup and recurring gotchas.

## Quick start

```bash
# 1. Build the generated Java project (Phase 2a — compile)
cd java.generated.project && ./gradlew build        # gradlew.bat on Windows

# 2. Run the whole pipeline for the active study (one iteration, all 12 phases)
python experiments/scripts/run_experiment_iteration.py

# 3. Run a single phase by name (authoritative status: forge.assets/corrections/post_<phase>.json)
python experiments/scripts/run_experiment_iteration.py --phases fdr4
python experiments/scripts/run_experiment_iteration.py --phases vacuity
```

The individual transformation commands (T2M / M2M / M2T / Dafny gen / Isabelle
gen, run from `forge.transformations/` via Gradle) are listed in
[`CLAUDE.md`](CLAUDE.md) → "Pipeline Commands".

## Where to go next

- [`CLAUDE.md`](CLAUDE.md) — the Java codegen constraints and M2M conventions you
  must follow for the transformation pipeline to extract a correct formal model.
- [`experiments/`](experiments/) — the convergence + cold-baseline experiment
  results, the run guides, and the reproducibility scripts.
- [`pipeline.yaml`](pipeline.yaml) — the authoritative definition of every phase.
