package devicegate.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by xiaoke on 17-6-7.
 */
public class SessionIdGenUtil {

    private static final Random ran = new Random();

    private static final AtomicLong gen = new AtomicLong(0);

    public static long genSessionId() {
        long nowCount = gen.getAndIncrement();
        long nowTime = System.currentTimeMillis();
        long nowRan = ran.nextLong();
        long genId = (nowCount & 0x0000007F) << 24;
        genId |= (nowRan & 0x000000FF) << 16;
        genId |= (nowTime & 0x0000FFFF);
        return genId;
    }
}
