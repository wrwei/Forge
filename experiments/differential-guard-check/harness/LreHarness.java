package harness;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import lre.actuator.Actuator;
import lre.controller.LreController;
import lre.mode.LreMode;
import lre.event.InputEvent;
import lre.operation.*;
import lre.sensor.Obstacle;
import lre.sensor.ObstacleRegister;
import lre.sensor.Sensor;

/** One controller step per input line; see ../README.md for the line format. */
public final class LreHarness {
    static String h(double d) { return Double.toHexString(d); }

    static Object get(Object o, String f) throws Exception {
        Field x = o.getClass().getDeclaredField(f); x.setAccessible(true); return x.get(o);
    }

    public static void main(String[] a) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        for (String line; (line = in.readLine()) != null; ) {
            String[] t = line.trim().split("\\s+");
            if (t.length < 8) continue;
            int k = 0;
            String id = t[k++], mode = t[k++], ev = t[k++];
            double depth = Double.parseDouble(t[k++]), ns = Double.parseDouble(t[k++]),
                   ew = Double.parseDouble(t[k++]), roc = Double.parseDouble(t[k++]);
            int n = Integer.parseInt(t[k++]);
            ObstacleRegister reg = new ObstacleRegister();
            for (int i = 0; i < n; i++) {
                int idx = Integer.parseInt(t[k++]);
                reg.put(idx, new Obstacle(Double.parseDouble(t[k++]), Double.parseDouble(t[k++]), Double.parseDouble(t[k++]),
                        Double.parseDouble(t[k++]), Double.parseDouble(t[k++]), Double.parseDouble(t[k++])));
            }
            Sensor s = new Sensor(reg);
            s.update(depth, ns, ew, roc);
            CalcVel cv = new CalcVel(s); CalcCStc cs = new CalcCStc(s); CalcCDyn cd = new CalcCDyn(s);
            CalcCPA cp = new CalcCPA(s, cd); CheckOPEZ co = new CheckOPEZ(s, cs);
            LreController c = new LreController(s, new Actuator(), cv, cs, cd, cp, co);
            Field fm = LreController.class.getDeclaredField("currentMode"); fm.setAccessible(true);
            fm.set(c, LreMode.valueOf(mode));
            InputEvent e = switch (ev) {
                case "reqVel" -> new InputEvent.ReqVel(0.5);
                case "reqHdng" -> new InputEvent.ReqHdng(0.5);
                case "reqOCM" -> new InputEvent.ReqOCM();
                case "reqMOM" -> new InputEvent.ReqMOM();
                case "reqHCM" -> new InputEvent.ReqHCM();
                case "endTask" -> new InputEvent.EndTask();
                default -> null;
            };
            c.step(e);
            out.append(id).append(' ').append(c.currentMode().name());
            for (String f : new String[]{"hvel", "vvel", "vel", "cda", "tcpa"}) out.append(' ').append(f).append('=').append(h((Double) get(c, f)));
            for (String f : new String[]{"cstc", "cdyn"}) out.append(' ').append(f).append('=').append(get(c, f));
            for (String f : new String[]{"inOpez", "camActive", "hcmActive", "momReturn"}) out.append(' ').append(f).append('=').append(get(c, f));
            out.append(" F:depth_fn=").append(h(s.depth())).append(" F:ns_vel=").append(h(s.ns_vel()))
               .append(" F:ew_vel=").append(h(s.ew_vel())).append(" F:rate_of_climb=").append(h(s.rate_of_climb()))
               .append(" F:closestStaticIndex=").append(s.closestStaticIndex())
               .append(" F:closestDynamicIndex=").append(s.closestDynamicIndex());
            java.util.TreeSet<Integer> idxs = new java.util.TreeSet<>(java.util.List.of(-1, s.closestStaticIndex(), s.closestDynamicIndex()));
            for (int i : idxs) {
                out.append(" F:odist:").append(i).append('=').append(h(s.odist(i)))
                   .append(" F:hdist:").append(i).append('=').append(h(s.hdist(i)))
                   .append(" F:vdist:").append(i).append('=').append(h(s.vdist(i)))
                   .append(" F:nsRelDist:").append(i).append('=').append(h(s.nsRelDist(i)))
                   .append(" F:ewRelDist:").append(i).append('=').append(h(s.ewRelDist(i)))
                   .append(" F:obsNsVel:").append(i).append('=').append(h(s.obsNsVel(i)))
                   .append(" F:obsEwVel:").append(i).append('=').append(h(s.obsEwVel(i)));
            }
            out.append('\n');
        }
        System.out.print(out);
    }
}
