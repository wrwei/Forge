package harness;
import java.io.*;
import java.util.List;
import chemdetector.data.GasSensor;
import chemdetector.data.Status;
import chemdetector.event.EventBus;
import chemdetector.event.GasAnalysisEvent;
import chemdetector.gasanalysis.GasAnalysisController;
import chemdetector.gasanalysis.GasAnalysisMode;
/** line: id mode event sts insVal */
public final class GasHarness {
    public static void main(String[] a) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); StringBuilder out = new StringBuilder();
        for (String line; (line = in.readLine()) != null; ) {
            String[] t = line.trim().split("\\s+"); if (t.length < 5) continue;
            GasAnalysisController c = new GasAnalysisController(new EventBus());
            Refl.set(c, "currentMode", GasAnalysisMode.valueOf(t[1]));
            Refl.set(c, "sts", Status.valueOf(t[3]));
            double ins = Double.parseDouble(t[4]); Refl.set(c, "insVal", ins);
            GasAnalysisEvent e = switch (t[2]) {
                case "gas" -> new GasAnalysisEvent.Gas(List.of(new GasSensor(1, 3.0), new GasSensor(2, 12.0)));
                case "tick" -> new GasAnalysisEvent.Tick(); default -> null; };
            c.step(e);
            out.append(t[0]).append(' ').append(c.currentMode().name()).append(" S:sts=").append(t[3])
               .append(" S:insVal=").append(Refl.h(ins)).append('\n');
        }
        System.out.print(out);
    }
}
