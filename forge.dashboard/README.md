# Pipeline Dashboard

Web UI for the formal-method-guided pipeline. See
[../CLAUDE.md](../CLAUDE.md) for the broader project context.

## Configuring verifier tool paths

The pipeline shells out to three external verifiers — **FDR4** (CSP
refinement), **Dafny** (deductive verification), and **Isabelle/UTP**
(theorem proving). For installation, see the per-tool guides next to
`pipeline.yaml`:
[INSTALL_FDR4.md](../INSTALL_FDR4.md),
[INSTALL_DAFNY.md](../INSTALL_DAFNY.md), and
[INSTALL_ISABELLE_WSL.md](../INSTALL_ISABELLE_WSL.md).

Their locations are configured in one place,
[../pipeline.yaml](../pipeline.yaml), under the corresponding phase
blocks. Each tool path is an OS-keyed map (`win32` / `darwin` / `linux`,
matching Python's `sys.platform`); the runner selects the entry for the
current OS.

A value is resolved as follows: a **bare binary name** (e.g. `dafny`,
`refines`, `isabelle`) is looked up on your `PATH`, so if the tool is on
your `PATH` no further configuration is needed; an **absolute path** is
used verbatim, for tools installed outside `PATH` (common on Windows).

```yaml
# Phase 6b — FDR4
fdr4_path:
  darwin: refines                                  # on PATH
  linux: refines
  win32: "C:\\Program Files\\fdr\\bin\\refines.exe" # absolute (edit to your install)
  # memory_limit_mb / timeout also live here

# Phase 6a — Dafny
dafny_path:
  darwin: dafny
  linux: dafny
  win32: "C:\\path\\to\\Dafny.exe"                  # edit to your install

# Phase 6c — Isabelle/UTP
isabelle_path:
  darwin: isabelle
  linux: isabelle
  win32: wsl.exe              # Windows runs Isabelle inside WSL
wsl_distro: Ubuntu            # which WSL distro (win32 only)
wsl_isabelle_bin: /home/<you>/isabelle/Isabelle2023-CyPhyAssure/bin/isabelle
```

**Edit these to match your machine before running the verification
phases.** On Windows, Isabelle runs inside WSL: set `wsl_distro` to your
distribution and `wsl_isabelle_bin` to the absolute path of the
`isabelle` launcher *inside* that distro (see
[../docs/design/isabelle_wsl_setup.md](../docs/design/isabelle_wsl_setup.md)).
The committed values are examples from the authors' setup; replace the
absolute paths with your own. The Java codegen, T2M, M2M, and M2T phases
need no external verifier and run without this configuration.

## Claude chat

The chat input at the bottom of the log panel sends prompts to a real
Claude Code session running server-side. Configure the `claude` binary
path in [../pipeline.yaml](../pipeline.yaml) under the `agent:` block.

Each destructive tool call (Edit / Write / Bash) triggers a modal for
your approval. The chat is a single persistent session, surviving
browser refreshes and dashboard restarts. Click `+ New` to rotate the
session id (old conversation history is preserved on disk by Claude Code
but no longer referenced).

## Requirements selection

The right-panel **Requirements** tab lets you pick which requirement
JSON files Claude should work from. The case-study dropdown switches
the active study (persisted to `pipeline.yaml`). "Load selected →
Claude" sends a single message naming the files; Claude reads them via
the auto-allowed Read tool.

The `<study>/system/system_description.txt` is always referenced as
context, regardless of which tiers are checked.
