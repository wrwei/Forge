package harness;
import java.lang.reflect.Field;
final class Refl {
    static Field f(Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x; }
    static Object get(Object o, String n) throws Exception { return f(o.getClass(), n).get(o); }
    static void set(Object o, String n, Object v) throws Exception { f(o.getClass(), n).set(o, v); }
    static String h(double d) { return Double.toHexString(d); }
}
