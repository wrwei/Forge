#!/usr/bin/env bash
#
# Drop-in replacement for the `isabelle` command on hosts where the
# Isabelle2023-CyPhyAssure distribution cannot run natively.
#
# The distribution ships Poly/ML only for x86_64-linux / x86_64_32-linux, and
# its prebuilt heaps are polyml-5.9_x86_64_32-linux. On macOS the launcher
# detects arm64-darwin, finds no ML runtime, and every ML-requiring command
# fails — `isabelle build`, `isabelle sessions`, everything except the few
# pure-shell subcommands like `version`. This wrapper runs the same
# distribution inside a linux/amd64 container instead.
#
# It is argument-transparent: `isabelle-docker.sh build -D /path -v` behaves
# like `isabelle build -D /path -v`. Host paths under $HOME and /tmp resolve
# to the same absolute paths inside the container, so callers need no
# translation. That is what lets pipeline.yaml point `isabelle_path` straight
# at this script.
#
# Usage:
#   scripts/isabelle-docker.sh version
#   scripts/isabelle-docker.sh sessions -a
#   scripts/isabelle-docker.sh build -D forge.transformations/output/isabelle -v -o timeout=600
#
# Env overrides:
#   ISABELLE_DIST   host path to the distribution (default ~/Isabelle2023-CyPhyAssure)
#   ISABELLE_IMAGE  image tag to use            (default forge/isabelle:2023-cyphyassure)
set -euo pipefail

ISABELLE_DIST="${ISABELLE_DIST:-$HOME/Isabelle2023-CyPhyAssure}"
# Heap ceiling. The directory mounted at /isabelle-user is NOT where the
# Isabelle distribution reads user settings (its etc/settings reassigns
# ISABELLE_HOME_USER to $USER_HOME/.isabelle/Isabelle2023 inside the
# container), so an ML_OPTIONS line placed there has no effect and Poly/ML
# runs with no --maxheap. To cap the heap use
# experiments/environment-model/mutation-modelB/isabelle-docker-maxheap.sh,
# which mounts the settings where they are read.
ISABELLE_IMAGE="${ISABELLE_IMAGE:-forge/isabelle:2023-cyphyassure}"
# Built heaps live here so they survive container exit. Without this, every
# invocation rebuilds any session not already in the distribution's heaps.
ISABELLE_USER_DIR="${ISABELLE_USER_DIR:-$HOME/.isabelle-docker}"

if [ ! -x "$ISABELLE_DIST/bin/isabelle" ]; then
  echo "isabelle-docker: no distribution at $ISABELLE_DIST" >&2
  echo "  Extract Isabelle2023-CyPhyAssure there, or set ISABELLE_DIST." >&2
  exit 2
fi

if ! docker info >/dev/null 2>&1; then
  echo "isabelle-docker: Docker is not running — start Docker Desktop." >&2
  exit 2
fi

if ! docker image inspect "$ISABELLE_IMAGE" >/dev/null 2>&1; then
  echo "isabelle-docker: building $ISABELLE_IMAGE (one-off, ~1 min)..." >&2
  docker build --platform linux/amd64 \
    -t "$ISABELLE_IMAGE" \
    "$(cd "$(dirname "${BASH_SOURCE[0]}")/../docker/isabelle" && pwd)" >&2
fi

mkdir -p "$ISABELLE_USER_DIR"

# $HOME and /tmp are mounted at identical paths so host-absolute arguments
# resolve unchanged inside the container. Note this gives the container
# read-write access to the home directory; it needs write access because
# `isabelle build` emits logs and artefacts beside the session it is given.
exec docker run --rm --platform linux/amd64 \
  -v "$ISABELLE_DIST:/opt/isabelle" \
  -v "$ISABELLE_USER_DIR:/isabelle-user" \
  -v "$HOME:$HOME" \
  -v /tmp:/tmp \
  -w "$PWD" \
  "$ISABELLE_IMAGE" "$@"
