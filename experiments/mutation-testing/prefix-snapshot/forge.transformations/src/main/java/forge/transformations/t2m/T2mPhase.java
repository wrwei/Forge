package forge.transformations.t2m;

import forge.transformations.t2m.SpoonDiscoverer;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import java.nio.file.Files;
import java.nio.file.Path;

import forge.transformations.core.Phase;
import forge.transformations.core.PhaseContext;

public final class T2mPhase implements Phase {
    @Override
    public void run(PhaseContext ctx) throws Exception {
        Path sourcePath = ctx.argPath("source");
        Path outputDir = ctx.argPath("output");
        Files.createDirectories(outputDir);

        System.out.println("Discovering Java sources in: " + sourcePath);
        SpoonDiscoverer discoverer = new SpoonDiscoverer();
        Resource resource = discoverer.discover(sourcePath);

        Path outputPath = outputDir.resolve("discovered_model.xmi");
        resource.setURI(URI.createFileURI(outputPath.toAbsolutePath().toString()));
        resource.save(null);
        System.out.println("Model saved to: " + outputPath.toAbsolutePath());

        discoverer.writeTraceFile(outputDir);

        int types = 0, methods = 0, fields = 0;
        for (var it = resource.getAllContents(); it.hasNext(); ) {
            EObject obj = it.next();
            String cn = obj.eClass().getName();
            switch (cn) {
                case "CtClass", "CtInterface", "CtEnum", "CtAnnotationType" -> types++;
                case "CtMethod", "CtConstructor" -> methods++;
                case "CtField" -> fields++;
            }
        }
        System.out.printf("Summary: %d types, %d methods, %d fields%n", types, methods, fields);

        ctx.put("java_resource", resource);
        ctx.put("source_positions", discoverer.getSourcePositionMap());
        ctx.put("source_positions_by_name", discoverer.getSourcePositionsByName());
        ctx.put("resolved_values", discoverer.getResolvedValues());
        ctx.put("record_metadata", discoverer.getRecordMetadata());
        ctx.put("constant_defaults", discoverer.getConstantDefaults());
    }
}
