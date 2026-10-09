package forge.transformations.core;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.emc.emf.InMemoryEmfModel;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.etl.EtlModule;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrates Epsilon ETL model-to-model transformations.
 * Runs .etl scripts against EMF models to produce higher-level
 * representations in the chain: Java EMF &rarr; KDM &rarr; UML &rarr; SysML.
 */
public class EtlTransformRunner {

    /**
     * Execute an ETL transformation script.
     *
     * @param etlScript       path to the .etl transformation file
     * @param sourceModelName name the source model is referred to in the ETL script
     * @param sourceModel     the input EMF resource
     * @param sourceMetamodel the source EPackage
     * @param targetModelName name the target model is referred to in the ETL script
     * @param targetMetamodel the target EPackage
     * @return the output EMF resource produced by the transformation
     * @throws Exception if parsing or execution fails
     */
    public Resource run(Path etlScript,
                        String sourceModelName, Resource sourceModel, EPackage sourceMetamodel,
                        String targetModelName, EPackage targetMetamodel) throws Exception {
        return run(etlScript, sourceModelName, sourceModel, sourceMetamodel,
                   targetModelName, targetMetamodel, Collections.emptyMap());
    }

    /**
     * Execute an ETL transformation script with injected variables.
     *
     * @param etlScript       path to the .etl transformation file
     * @param sourceModelName name the source model is referred to in the ETL script
     * @param sourceModel     the input EMF resource
     * @param sourceMetamodel the source EPackage
     * @param targetModelName name the target model is referred to in the ETL script
     * @param targetMetamodel the target EPackage
     * @param variables       name-value pairs to inject as read-only variables into the ETL context
     * @return the output EMF resource produced by the transformation
     * @throws Exception if parsing or execution fails
     */
    public Resource run(Path etlScript,
                        String sourceModelName, Resource sourceModel, EPackage sourceMetamodel,
                        String targetModelName, EPackage targetMetamodel,
                        Map<String, Object> variables) throws Exception {

        EtlModule module = new EtlModule();
        module.parse(etlScript.toUri());

        if (!module.getParseProblems().isEmpty()) {
            String problems = module.getParseProblems().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
            throw new IllegalArgumentException("ETL parse errors in " + etlScript + ":\n" + problems);
        }

        // Wrap source model
        InMemoryEmfModel source = new InMemoryEmfModel(sourceModelName, sourceModel, sourceMetamodel);
        source.setReadOnLoad(false);
        source.setStoredOnDisposal(false);

        // Create empty target resource and wrap
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("xmi", new XMIResourceFactoryImpl());
        Resource targetResource = rs.createResource(URI.createURI(targetModelName + "_output.xmi"));

        InMemoryEmfModel target = new InMemoryEmfModel(targetModelName, targetResource, targetMetamodel);
        target.setReadOnLoad(false);
        target.setStoredOnDisposal(false);

        // Register models
        module.getContext().getModelRepository().addModel(source);
        module.getContext().getModelRepository().addModel(target);

        // Inject variables into the ETL execution context
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                module.getContext().getFrameStack().put(
                        Variable.createReadOnlyVariable(entry.getKey(), entry.getValue()));
            }
        }

        try {
            module.execute();
        } finally {
            module.getContext().getModelRepository().dispose();
        }

        return targetResource;
    }
}
