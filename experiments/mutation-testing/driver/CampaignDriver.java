import forge.transformations.core.PhaseContext;
import forge.transformations.t2m.T2mPhase;
import forge.transformations.m2m.TransformPhase;
import forge.transformations.m2t.DafnyPhase;
import forge.transformations.m2t.RctPhase;
import forge.transformations.m2t.IsabellePhase;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Campaign driver: runs the pre-fix extractor pipeline
 * (T2M -> M2M -> Dafny/RCT/Isabelle generation) on one Java source tree.
 * Mirrors pipeline.yaml phase order; all metadata (resolved values,
 * record metadata, constant defaults, source positions) flows through
 * the shared PhaseContext exactly as in the production pipeline.
 *
 * Usage: CampaignDriver <javaSourceDir> <outputDir>
 */
public final class CampaignDriver {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: CampaignDriver <javaSourceDir> <outputDir>");
            System.exit(2);
        }
        Map<String, Object> a = new HashMap<>();
        a.put("source", args[0]);
        a.put("output", args[1]);
        PhaseContext ctx = new PhaseContext(Path.of(args[1]), a);

        new T2mPhase().run(ctx);        // discovery + resolved values
        new TransformPhase().run(ctx);  // ETL -> RoboChart, constant_defaults.json
        new DafnyPhase().run(ctx);      // .dfy
        new RctPhase().run(ctx);        // .rct (reads constant_defaults.json)
        new IsabellePhase().run(ctx);   // isabelle/<Stm>_Beh.thy + ROOT
        System.out.println("CAMPAIGN_DRIVER_OK");
    }
}
