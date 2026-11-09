package harness;
import java.io.*;
import sranger.actuator.Actuator;
import sranger.controller.SRangerController;
import sranger.event.InputEvent;
import sranger.mode.SRangerMode;
import sranger.operation.TurnTimer;
import sranger.sensor.Sensor;
import sranger.timing.Clock;
/** line: id mode event distance elapsedFlag(0|1) */
public final class SRangerHarness {
    public static void main(String[] a) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); StringBuilder out = new StringBuilder();
        for (String line; (line = in.readLine()) != null; ) {
            String[] t = line.trim().split("\\s+"); if (t.length < 5) continue;
            Sensor s = new Sensor(); s.updateDistance(Double.parseDouble(t[3]));
            TurnTimer tt = new TurnTimer(new Clock());
            Refl.set(tt, "resetTime", t[4].equals("1") ? -1e300 : 1e300);
            SRangerController c = new SRangerController(s, new Actuator(), tt);
            Refl.set(c, "currentMode", SRangerMode.valueOf(t[1]));
            InputEvent e = switch (t[2]) { case "obstacle" -> new InputEvent.Obstacle(); case "tick" -> new InputEvent.Tick();
                case "endTask" -> new InputEvent.EndTask(); default -> null; };
            c.step(e);
            out.append(t[0]).append(' ').append(((SRangerMode) Refl.get(c, "currentMode")).name())
               .append(" turnDurationElapsed=").append(Refl.get(c, "turnDurationElapsed"))
               .append(" F:distance=").append(Refl.h(s.distance())).append('\n');
        }
        System.out.print(out);
    }
}
