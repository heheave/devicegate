package devicegate.util;

/**
 * Created by xiaoke on 17-11-13.
 */
public class SystemStateMonitorUtil {

    private static final ResourceUsage cu = CpuUsage.getInstance();
    private static final ResourceUsage mu = MemUsage.getInstance();
    private static final ResourceUsage iu = IoUsage.getInstance();
    private static final ResourceUsage nu = NetUsage.getInstance();

    public static float getCpuUsage() {
        return cu.get();
    }

    public static float getMemUsage() {
        return mu.get();
    }

    public static float getIoUsage() {
        return iu.get();
    }

    public static float getNetUsage() {
        return nu.get();
    }
}
