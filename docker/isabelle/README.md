# Isabelle2023-CyPhyAssure in Docker (for macOS hosts)

Phase 6c (`isabelle_verify`) needs the CyPhyAssure Isabelle distribution,
which supplies the `Z_Machines` session the generated `<Stm>_Beh.thy`
theories import. This directory makes that runnable on macOS.

## Why a container is necessary

The distribution is **Linux-only**, even though nothing about it says so:

- `contrib/polyml-219e0a248f70/` contains only `x86_64-linux` and
  `x86_64_32-linux` — there is no darwin build of the ML runtime.
- The prebuilt heaps are `heaps/polyml-5.9_x86_64_32-linux/`, including
  `Z_Machines`.
- Isabelle's launcher correctly detects the Mac as `arm64-darwin`, looks for
  an ML runtime that isn't there, and fails.

The failure is easy to misread. `isabelle version` prints `Isabelle2023`
happily, because it is a shell script reading a text file and never starts
ML. A couple of components (`rsync`, `bash_process`) even ship
`platform_x86_64-darwin` directories. The install looks healthy right up
until you run something real.

## Usage

`scripts/isabelle-docker.sh` is argument-transparent — use it anywhere you
would use `isabelle`:

```bash
scripts/isabelle-docker.sh version
scripts/isabelle-docker.sh sessions -a
scripts/isabelle-docker.sh build -D forge.transformations/output/isabelle -v -o timeout=600
```

The image builds itself on first use (~1 min). `pipeline.yaml` points
`phases.isabelle_verify.isabelle_path.darwin` at this script, so the
dashboard's phase 6c works with no further setup.

Host paths under `$HOME` and `/tmp` are mounted at identical paths inside the
container, so arguments like `-D <host path>` need no translation.
On macOS the resolved `/private/tmp` spelling is mounted as well, so paths
resolved by the Python worker still reach their generated session files. Built
heaps persist in `~/.isabelle-docker` — without that, every run would rebuild
the session stack from scratch.
The wrapper mounts that directory at `/root/.isabelle/Isabelle2023`, where this
distribution actually reads user settings and writes heaps. Setting an
`ISABELLE_HOME_USER` environment variable alone does not override its startup
settings.

### Environment overrides

| Variable | Default | Purpose |
|---|---|---|
| `ISABELLE_DIST` | `~/Isabelle2023-CyPhyAssure` | Host path to the distribution |
| `ISABELLE_IMAGE` | `forge/isabelle:2023-cyphyassure` | Image tag |
| `ISABELLE_USER_DIR` | `~/.isabelle-docker` | Persistent heaps / user settings |

## Two traps worth knowing

**Fonts are required despite headless operation.** `isabelle build` calls
`Isabelle_Fonts.init()` from `Build$Engine.build_store`, which calls
`java.awt.Font.createFont`. Without `fontconfig` / `libfreetype6` that throws,
and Isabelle reports only:

```
*** java.lang.InternalError: java.lang.reflect.InvocationTargetException
```

with the real cause swallowed. Worse, `isabelle sessions -a` does *not* touch
fonts, so it succeeds — making the container look correct while every build
fails. The Dockerfile installs the font libraries for exactly this reason;
don't trim them as "not needed for headless".

**TeX is deliberately absent.** The generated sessions set `document = false`,
so document preparation never runs. Installing `texlive` would add ~2 GB for
output that is never produced.

## Performance

The container runs `linux/amd64` under emulation on Apple Silicon. Measured
2026-08-15 on the LRE session: **71 s elapsed** (110 s CPU, 8 threads) for
`LreController_Check`, plus a one-off first run to build the `Z_Machines`
session stack. That is acceptable for the mutation campaign, but Isabelle
remains the slowest of the three verifiers — budget accordingly when
scheduling batches.

## If a native macOS build appears

Set `phases.isabelle_verify.isabelle_path.darwin` back to plain `isabelle`
and this whole directory becomes unnecessary.
