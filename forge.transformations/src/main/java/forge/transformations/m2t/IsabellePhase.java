package forge.transformations.m2t;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import forge.transformations.core.IsabelleEgxRunner;
import forge.transformations.m2m.RoboChartMetamodel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import forge.transformations.core.Phase;
import forge.transformations.core.PhaseContext;
import forge.transformations.m2m.TransformPhase;

/**
 * Phase: RoboChart EMF → Isabelle/UTP Z-Machine theory (.thy).
 *
 * <p>Reads the RoboChart resource from context (populated by {@link TransformPhase})
 * or loads {@code robochart_model.xmi} from disk. Writes
 * {@code <output>/isabelle/<StmName>_Beh.thy} and {@code <output>/isabelle/ROOT}.
 */
public final class IsabellePhase implements Phase {

    @Override
    public void run(PhaseContext ctx) throws Exception {
        Path outputDir = ctx.argPath("output");
        Files.createDirectories(outputDir);

        Resource rcResource;
        if (ctx.has("rc_resource")) {
            rcResource = ctx.require("rc_resource", Resource.class);
        } else {
            rcResource = loadRoboChartModel(outputDir);
        }

        System.out.println("Generating Isabelle Z-Machine theory...");
        try {
            IsabelleEgxRunner runner = new IsabelleEgxRunner();
            runner.run(rcResource, RoboChartMetamodel.getInstance().ePackage(), outputDir);
        } catch (Exception e) {
            throw new IOException("Isabelle theory generation failed: " + e.getMessage(), e);
        }
    }

    private static Resource loadRoboChartModel(Path outputDir) throws IOException {
        Path xmiPath = outputDir.resolve("robochart_model.xmi");
        if (!Files.exists(xmiPath)) {
            throw new IOException("robochart_model.xmi not found at "
                    + xmiPath.toAbsolutePath() + ". Run 'transform' first.");
        }
        RoboChartMetamodel.getInstance();
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("xmi", new XMIResourceFactoryImpl());
        Resource resource = rs.getResource(
                URI.createFileURI(xmiPath.toAbsolutePath().toString()), true);
        System.out.println("Loaded RoboChart model from: " + xmiPath.toAbsolutePath());
        return resource;
    }
}
