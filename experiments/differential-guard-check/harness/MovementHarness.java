package harness;
import java.io.*;
import chemdetector.data.Angle;
import chemdetector.data.Loc;
import chemdetector.event.MovementEvent;
import chemdetector.movement.MovementController;
import chemdetector.movement.MovementMode;
import chemdetector.operation.ChangeDirection;
import chemdetector.vehicle.Clock;
import chemdetector.vehicle.Vehicle;
/** line: id mode event now clockResetTime d0 d1 */
public final class MovementHarness {
    public static void main(String[] a) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); StringBuilder out = new StringBuilder();
        for (String line; (line = in.readLine()) != null; ) {
            String[] t = line.trim().split("\\s+"); if (t.length < 7) continue;
            Vehicle v = new Vehicle(); Clock clk = new Clock();
            long now = Long.parseLong(t[3]), crt = Long.parseLong(t[4]);
            double d0 = Double.parseDouble(t[5]), d1 = Double.parseDouble(t[6]);
            Refl.set(clk, "nowTicks", now);
            MovementController c = new MovementController(v, clk, new ChangeDirection(v));
            Refl.set(c, "currentMode", MovementMode.valueOf(t[1]));
            Refl.set(c, "clockResetTime", crt); Refl.set(c, "d0", d0); Refl.set(c, "d1", d1);
            MovementEvent e = switch (t[2]) {
                case "turn" -> new MovementEvent.Turn(Angle.Left); case "stop" -> new MovementEvent.Stop();
                case "resume" -> new MovementEvent.Resume(); case "obstacle" -> new MovementEvent.Obstacle(Loc.left);
                case "tick" -> new MovementEvent.Tick(); default -> null; };
            c.step(e);
            out.append(t[0]).append(' ').append(c.currentMode().name()).append(" S:d0=").append(Refl.h(d0)).append(" S:d1=").append(Refl.h(d1))
               .append(" F:clockResetTime=").append(crt).append(" NOW=").append(now).append('\n');
        }
        System.out.print(out);
    }
}
