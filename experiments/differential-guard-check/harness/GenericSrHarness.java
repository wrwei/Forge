package harness;

import java.io.*;
import java.lang.reflect.*;

/** Reflection-driven SRanger harness for the loop runs. line: id mode event distance now clockResetTime */
public final class GenericSrHarness {
    public static void main(String[] a) throws Exception {
        Class<?> ctl = Class.forName("sranger.controller.SRangerController");
        Constructor<?> cc = ctl.getConstructors()[0]; Class<?>[] pt = cc.getParameterTypes();
        Method upd = null; for (Method m : pt[0].getMethods()) if (m.getName().equals("update") && m.getParameterCount() == 1) upd = m;
        Method step = null; for (Method m : ctl.getMethods()) if (m.getName().equals("step")) step = m;
        Class<?> ev = step.getParameterTypes()[0];
        Field fm = Refl.f(ctl, "currentMode"); Class<?> modeEnum = fm.getType();
        Field settable = null;
        for (Field f : pt[2].getDeclaredFields()) if (f.getType() == double.class && !Modifier.isFinal(f.getModifiers())) settable = f;
        if (settable != null) settable.setAccessible(true);
        Method now = pt[2].getMethod("now");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); StringBuilder out = new StringBuilder();
        for (String line; (line = in.readLine()) != null; ) {
            String[] t = line.trim().split("\\s+"); if (t.length < 6) continue;
            Object s = pt[0].getConstructor().newInstance(); upd.invoke(s, Double.parseDouble(t[3]));
            Object clk = pt[2].getConstructor().newInstance();
            if (settable != null) settable.set(clk, Double.parseDouble(t[4]));
            Object c = cc.newInstance(s, pt[1].getConstructor().newInstance(), clk);
            Object mode = null; for (Object k : modeEnum.getEnumConstants()) if (((Enum<?>) k).name().equalsIgnoreCase(t[1])) mode = k;
            fm.set(c, mode);
            Refl.set(c, "clockResetTime", Double.parseDouble(t[5]));
            Object e = null;
            if (!t[2].equals("none")) for (Class<?> r : ev.getPermittedSubclasses()) if (r.getSimpleName().equalsIgnoreCase(t[2])) e = r.getDeclaredConstructors()[0].newInstance();
            double before = (Double) now.invoke(clk);
            step.invoke(c, e);
            Method dist = pt[0].getMethod("distance");
            out.append(t[0]).append(' ').append(((Enum<?>) fm.get(c)).name()).append(" F:distance=").append(Refl.h((Double) dist.invoke(s)))
               .append(" F:clockResetTime=").append(Refl.h(Double.parseDouble(t[5]))).append(" NOW=").append(Refl.h(before))
               .append(" SETTABLE=").append(settable != null).append('\n');
        }
        System.out.print(out);
    }
}
