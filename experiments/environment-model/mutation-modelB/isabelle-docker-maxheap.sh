#!/usr/bin/env bash
# isabelle-docker-maxheap.sh -- scripts/isabelle-docker.sh with a Poly/ML heap
# ceiling that actually takes effect.
#
# Why: the base wrapper sets ISABELLE_HOME_USER=/isabelle-user in the image, but
# the distribution's etc/settings reassigns ISABELLE_HOME_USER to
# $USER_HOME/.isabelle/$ISABELLE_IDENTIFIER (= /root/.isabelle/Isabelle2023 in
# the container). So ~/.isabelle-docker/etc/settings (ML_OPTIONS="--maxheap 6g")
# has never been read: every build ran with ML_OPTIONS="--minheap 500" and no
# ceiling, and Poly/ML grows until the Docker VM's kernel kills it (rc 137).
#
# This wrapper mounts a settings directory at the path Isabelle reads, holding
# ONLY the heap options (no ML_PLATFORM change: the shipped heaps are
# x86_64_32 and changing platform would rebuild HOL). Heap ceiling: B6_MAXHEAP
# (default 6g; keep it ~2 GB under Docker Desktop's memory limit).
set -euo pipefail
ISABELLE_DIST="${ISABELLE_DIST:-$HOME/Isabelle2023-CyPhyAssure}"
ISABELLE_IMAGE="${ISABELLE_IMAGE:-forge/isabelle:2023-cyphyassure}"
UD="${B6_USER_DIR:-$HOME/.isabelle-b6}"
mkdir -p "$UD/etc"
printf 'ML_OPTIONS="--minheap 500 --maxheap %s"\n' "${B6_MAXHEAP:-6g}" > "$UD/etc/settings"
exec docker run --rm --platform linux/amd64 \
  -v "$ISABELLE_DIST:/opt/isabelle" \
  -v "$UD:/root/.isabelle/Isabelle2023" \
  -v "$HOME:$HOME" \
  -v /tmp:/tmp \
  -w "$PWD" \
  "$ISABELLE_IMAGE" "$@"
