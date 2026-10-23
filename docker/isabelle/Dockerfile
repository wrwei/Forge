# Runtime for Isabelle2023-CyPhyAssure on non-Linux hosts.
#
# Why this exists: the CyPhyAssure distribution ships Poly/ML only for
# x86_64-linux / x86_64_32-linux (contrib/polyml-*/), and its prebuilt heaps
# are polyml-5.9_x86_64_32-linux. On macOS the launcher correctly detects
# arm64-darwin, finds no ML runtime, and every ML-requiring command fails —
# including `isabelle build` and even `isabelle sessions`. (`isabelle version`
# still works, which makes the install look healthy when it is not.)
#
# This image supplies a Linux userland; the distribution itself is BIND-MOUNTED
# at run time rather than copied in, so the image stays small and a distribution
# upgrade needs no rebuild.
#
# Must be run as linux/amd64 — on Apple Silicon that is emulated. The wrapper
# script passes --platform explicitly.
FROM --platform=linux/amd64 ubuntu:22.04

# Isabelle2023's documented Linux prerequisites, minus TeX. Document
# preparation is not needed: the generated sessions set `document = false`.
# Adding texlive would cost ~2 GB for output we never produce.
#
# fontconfig + libfreetype6 are NOT optional, despite headless operation.
# `isabelle build` calls Isabelle_Fonts.init() from Build$Engine.build_store,
# which calls java.awt.Font.createFont on the bundled Isabelle fonts. Without
# the native font engine that throws, and Isabelle reports only the unhelpful
# "java.lang.InternalError: java.lang.reflect.InvocationTargetException" —
# with the real cause swallowed. `isabelle sessions` does not touch fonts, so
# it succeeds and the install looks fine until you try to build.
RUN apt-get update -qq \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y -qq --no-install-recommends \
      curl \
      fontconfig \
      less \
      libfontconfig1 \
      libfreetype6 \
      libgomp1 \
      perl \
      procps \
      rlwrap \
      unzip \
 && rm -rf /var/lib/apt/lists/*

# Isabelle2023 computes ISABELLE_HOME_USER from the root user's home and
# its version identifier, overriding an environment value of the same name.
# The wrapper mounts /root/.isabelle/Isabelle2023 so settings and heaps persist.

# The distribution mount point. Keep in sync with the wrapper script.
ENV PATH=/opt/isabelle/bin:$PATH

WORKDIR /work
ENTRYPOINT ["isabelle"]
