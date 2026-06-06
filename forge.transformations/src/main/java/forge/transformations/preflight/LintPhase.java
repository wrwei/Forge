package forge.transformations.preflight;

import forge.transformations.preflight.StructuralLinter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import forge.transformations.core.Phase;
import forge.transformations.core.PhaseContext;

public final class LintPhase implements Phase {
    @Override
    public void run(PhaseContext ctx) throws Exception {
        Path sourcePath = ctx.argPath("source");
        Path outputDir = ctx.argPath("output");
        Files.createDirectories(outputDir);

        System.out.println("Linting Java sources in: " + sourcePath);
        StructuralLinter linter = new StructuralLinter();
        linter.lint(sourcePath);

        Path reportPath = outputDir.resolve("lint_report.json");
        linter.writeReport(reportPath);

        int errors = 0, warnings = 0;
        for (Map<String, Object> v : linter.getViolations()) {
            String sev = String.valueOf(v.get("severity"));
            if ("error".equals(sev)) errors++;
            else if ("warning".equals(sev)) warnings++;
        }
        System.out.printf("Lint: %d violation(s) (%d error(s), %d warning(s))%n",
                linter.getViolations().size(), errors, warnings);
        System.out.println("Report written to: " + reportPath.toAbsolutePath());

        ctx.put("lint_violations", linter.getViolations());
    }
}
