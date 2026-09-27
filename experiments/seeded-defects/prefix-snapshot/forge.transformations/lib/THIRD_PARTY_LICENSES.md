# Third-Party Licenses — `forge.transformations/lib/`

The JARs in this directory are **third-party binaries**, redistributed
here so the M2T / CSP-generation phase (`roboChartCspGen` in
`build.gradle`) runs without a separate RoboTool installation. They are
**not** covered by this repository's MIT `LICENSE`, which applies only to
the project's own source. Each dependency is governed by its own license,
as recorded in the `LICENSE` / `about.html` / `NOTICE` files inside the
respective JAR. This file summarises their provenance and license; consult
each JAR's embedded license for the authoritative text.

## RoboChart / RoboStar (University of York) — EPL-2.0

The CSP generator and its RoboChart support, from the RoboStar project
(<https://robostar.cs.york.ac.uk/>, <https://github.com/UoY-RoboStar>):

- `circus.robocalc.robochart_*.jar`
- `circus.robocalc.robochart.assertions_*.jar`, `…assertions.ide_*.jar`
- `circus.robocalc.robochart.textual_*.jar`, `…textual.ide_*.jar`
- `circus.robocalc.robochart.generator.csp_*.jar` (also under `robotool/`)

License: **Eclipse Public License v2.0** (verified in the jars' embedded
`LICENSE`). Copyright the University of York and RoboChart contributors.

## Eclipse Platform / EMF / Xtext / Equinox / MWE — EPL-2.0

Eclipse Foundation runtime libraries:

- `org.eclipse.core.*`, `org.eclipse.equinox.*`, `org.eclipse.osgi_*`
- `org.eclipse.emf.*` (ecore, common, codegen, xmi, mwe, mwe2)
- `org.eclipse.xtext*`, `org.eclipse.xtend.lib*`

License: **Eclipse Public License** (EPL-1.0/2.0 per the respective
`about.html` inside each jar). Copyright Eclipse Foundation and
contributors. Source availability per the EPL is from the Eclipse project.

## Apache-licensed dependencies — Apache License 2.0

- `com.google.guava_*.jar` — Google Guava
- `com.google.inject_*.jar` (Guice), `javax.inject_*.jar`
- `org.apache.log4j_1.2.15.*.jar` — Apache log4j 1.2

License: **Apache License 2.0**.

## ANTLR runtime — BSD-3-Clause

- `org.antlr.runtime_3.2.0.*.jar` — ANTLR 3 runtime.

License: **BSD 3-Clause**.

## Note for redistributors

EPL and Apache-2.0 both permit redistribution with attribution and
preservation of license/notice files (which remain inside each JAR). If
you fork or repackage this repository, keep this file and the embedded
license files intact. If you prefer not to redistribute these binaries,
delete `forge.transformations/lib/` and obtain the RoboTool CSP generator
separately (see <https://robostar.cs.york.ac.uk/robotool/>), pointing
`build.gradle`'s `roboChartCspGen` task at your own copy.
