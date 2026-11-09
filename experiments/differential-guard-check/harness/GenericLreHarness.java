package harness;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Reflection-driven LRE harness for the loop runs, whose class layouts differ in detail.
 * Mode "sig": prints the Sensor.update parameter names and the obstacle record's components.
 * Otherwise each line is: id mode event u1..uk n [idx c1..cm]*  (values in the order "sig" printed).
 */
public final class GenericLreHarness {
    static Class<?> ctl, sensor, reg, obs, actuator, modeEnum, events;
    static Method update, put;

    static void discover() throws Exception {
        ctl = Class.forName("lre.controller.LreController");
        Constructor<?> cc = ctl.getConstructors()[0];
        sensor = cc.getParameterTypes()[0]; actuator = cc.getParameterTypes()[1];
        reg = sensor.getConstructors()[0].getParameterTypes()[0];
        for (Method m : sensor.getMethods()) if (m.getName().equals("update")) update = m;
        for (Method m : reg.getMethods()) {
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 2 && p[0] == int.class && p[1].isRecord()) { put = m; obs = p[1]; }
        }
        modeEnum = Refl.f(ctl, "currentMode").getType();
        for (Class<?> c : ctl.getMethods()[0].getDeclaringClass().getDeclaredMethods()[0].getDeclaringClass().getDeclaredClasses()) {}
        for (Method m : ctl.getMethods()) if (m.getName().equals("step")) events = m.getParameterTypes()[0];
    }

    static Object event(String name) throws Exception {
        if (name.equals("none")) return null;
        for (Class<?> r : events.getPermittedSubclasses()) {
            if (r.getSimpleName().equalsIgnoreCase(name)) {
                Constructor<?> k = r.getDeclaredConstructors()[0]; Object[] a = new Object[k.getParameterCount()];
                for (int i = 0; i < a.length; i++) a[i] = 0.5;
                return k.newInstance(a);
            }
        }
        throw new IllegalArgumentException(name);
    }

    public static void main(String[] a) throws Exception {
        discover();
        if (a.length > 0 && a[0].equals("sig")) {
            StringBuilder s = new StringBuilder("update");
            for (Parameter p : update.getParameters()) s.append(' ').append(p.getName());
            s.append("\nobstacle");
            for (RecordComponent rc : obs.getRecordComponents()) s.append(' ').append(rc.getName());
            s.append("\nmethods");
            for (Method m : sensor.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers()) && (m.getReturnType() == double.class || m.getReturnType() == int.class) && m.getParameterCount() <= 1) s.append(' ').append(m.getName()).append('/').append(m.getParameterCount());
            s.append("\nfields");
            for (Field f : ctl.getDeclaredFields()) if (f.getType() == double.class || f.getType() == int.class || f.getType() == boolean.class) s.append(' ').append(f.getName());
            System.out.println(s); return;
        }
        int ku = update.getParameterCount(), ko = obs.getRecordComponents().length;
        Constructor<?> oc = obs.getDeclaredConstructors()[0];
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); StringBuilder out = new StringBuilder();
        for (String line; (line = in.readLine()) != null; ) {
            String[] t = line.trim().split("\\s+"); if (t.length < 4) continue;
            int k = 3; Object r = reg.getConstructor().newInstance(); Object s = sensor.getConstructors()[0].newInstance(r);
            Object[] ua = new Object[ku]; for (int i = 0; i < ku; i++) ua[i] = Double.parseDouble(t[k++]);
            update.invoke(s, ua);
            int n = Integer.parseInt(t[k++]);
            for (int i = 0; i < n; i++) {
                int idx = Integer.parseInt(t[k++]); Object[] oa = new Object[ko];
                for (int j = 0; j < ko; j++) oa[j] = Double.parseDouble(t[k++]);
                put.invoke(r, idx, oc.newInstance(oa));
            }
            Object c = ctl.getConstructors()[0].newInstance(s, actuator.getConstructor().newInstance());
            Field fm = Refl.f(ctl, "currentMode"); fm.set(c, Enum.valueOf((Class) modeEnum, t[1]));
            Method step = null; for (Method m : ctl.getMethods()) if (m.getName().equals("step")) step = m;
            step.invoke(c, event(t[2]));
            out.append(t[0]).append(' ').append(((Enum<?>) fm.get(c)).name());
            for (Field f : ctl.getDeclaredFields()) {
                if (f.getType() == double.class) { f.setAccessible(true); out.append(' ').append(f.getName()).append('=').append(Refl.h(f.getDouble(c))); }
                else if (f.getType() == int.class || f.getType() == boolean.class) { f.setAccessible(true); out.append(' ').append(f.getName()).append('=').append(f.get(c)); }
            }
            TreeSet<Integer> idxs = new TreeSet<>(List.of(-1));
            for (Method m : sensor.getDeclaredMethods()) {
                if (!Modifier.isPublic(m.getModifiers()) || m.getParameterCount() != 0) continue;
                if (m.getReturnType() == double.class) out.append(" F:").append(m.getName()).append('=').append(Refl.h((Double) m.invoke(s)));
                else if (m.getReturnType() == int.class) { int v = (Integer) m.invoke(s); idxs.add(v); out.append(" F:").append(m.getName()).append('=').append(v); }
            }
            for (Method m : sensor.getDeclaredMethods()) {
                if (!Modifier.isPublic(m.getModifiers()) || m.getParameterCount() != 1 || m.getParameterTypes()[0] != int.class || m.getReturnType() != double.class) continue;
                for (int i : idxs) out.append(" F:").append(m.getName()).append(':').append(i).append('=').append(Refl.h((Double) m.invoke(s, i)));
            }
            out.append('\n');
        }
        System.out.print(out);
    }
}
