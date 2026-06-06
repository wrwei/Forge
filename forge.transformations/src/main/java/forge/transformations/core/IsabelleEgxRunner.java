package forge.transformations.core;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Orchestrates Isabelle/UTP Z-Machine theory generation via the
 * forked EGL template at
 * {@code transformations/thy_generation_rule.egl} (loaded from the
 * runtime classpath, same pattern as the Dafny / RCT runners).
 *
 * <p>The template is derived from {@code thy_generation_rule.egl} in the
 * ICECCS2023 archive (vendored at {@code docs/archive/ICECCS2023/}),
 * with local patches applied for bugs the archive authors did not hit.
 * See {@code docs/fixes/I2_template_fork.md} for the diff against
 * upstream and the rationale for each patch.
 *
 * <p>Outputs:
 * <ul>
 *   <li>{@code <output>/isabelle/<StmName>_Beh.thy} — generated theory</li>
 *   <li>{@code <output>/isabelle/ROOT} — session file declaring
 *       {@code session <StmName>_Check = "Z_Machines" + theories
 *       <StmName>_Beh} so that {@code isabelle build} can verify it</li>
 * </ul>
 */
public class IsabelleEgxRunner {

    private static final String EGL_RESOURCE = "transformations/thy_generation_rule.egl";

    /**
     * Generate the Isabelle theory and its ROOT session file.
     *
     * @param rcResource   the RoboChart EMF model (from ETL transform output)
     * @param rcMetamodel  the RoboChart EPackage
     * @param outputDir    directory to place generated files
     * @return the path of the generated {@code .thy} file
     */
    public Path run(Resource rcResource, EPackage rcMetamodel,
                    Path outputDir) throws Exception {
        String stmName = findStateMachineName(rcResource);
        if (stmName == null || stmName.isBlank()) {
            throw new IllegalStateException(
                "No StateMachineDef found in RoboChart model; cannot derive stm_name");
        }

        URL eglUrl = getClass().getClassLoader().getResource(EGL_RESOURCE);
        if (eglUrl == null) {
            throw new IllegalStateException("Cannot find " + EGL_RESOURCE + " on classpath");
        }
        Path eglPath = Path.of(eglUrl.toURI());

        Path isabelleDir = outputDir.resolve("isabelle");
        Files.createDirectories(isabelleDir);
        Path thyPath = isabelleDir.resolve(stmName + "_Beh.thy");

        Map<String, Object> vars = new HashMap<>();
        vars.put("stm_name", stmName);

        EglGenerationRunner runner = new EglGenerationRunner();
        runner.run(eglPath, "RC", rcResource, rcMetamodel, thyPath, vars);

        writeRootFile(isabelleDir, stmName);

        System.out.println("Isabelle theory written to: " + thyPath.toAbsolutePath());
        return thyPath;
    }

    /**
     * Walk the resource to find the first StateMachineDef's name.
     */
    private static String findStateMachineName(Resource rcResource) {
        for (var it = rcResource.getAllContents(); it.hasNext(); ) {
            EObject obj = it.next();
            if ("StateMachineDef".equals(obj.eClass().getName())) {
                EStructuralFeature nameFeature = obj.eClass().getEStructuralFeature("name");
                if (nameFeature != null) {
                    Object name = obj.eGet(nameFeature);
                    if (name != null) return name.toString();
                }
            }
        }
        return null;
    }

    /**
     * Write a minimal ROOT file declaring a session that inherits from
     * the CyPhyAssure {@code Z_Machines} heap. {@code isabelle build}
     * uses this to locate and verify the theory non-interactively.
     *
     * <p>Note: the parent session is named {@code Z_Machines} (plural),
     * declared in {@code Isabelle2023-CyPhyAssure/src/CyPhyAssure/Z_Machines/ROOT}.
     * The theory inside it (referenced from the generated .thy as
     * {@code imports "Z_Machines.Z_Machine"}) is singular, hence the
     * historical confusion.
     */
    private static void writeRootFile(Path isabelleDir, String stmName) throws IOException {
        String content =
            "session " + stmName + "_Check = \"Z_Machines\" +\n" +
            "  theories\n" +
            "    " + stmName + "_Beh\n";
        Path rootPath = isabelleDir.resolve("ROOT");
        Files.writeString(rootPath, content);
    }
}
