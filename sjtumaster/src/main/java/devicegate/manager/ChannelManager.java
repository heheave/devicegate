package devicegate.manager;


import io.netty.channel.Channel;
import org.apache.log4j.Logger;

/**
 * Created by xiaoke on 17-5-16.
 */
public class ChannelManager extends AbstactManager<Channel>{

    private static final Logger log = Logger.getLogger(ChannelManager.class);

    private static ChannelManager cm = null;

    @Override
    void afterRemoved(Channel oldValue) {
        if (oldValue != null) {
            oldValue.close();
        }
    }

    public static ChannelManager getInstance() {
        if (cm == null) {
            synchronized (ChannelManager.class) {
                if (cm == null) {
                    cm = new ChannelManager();
                }
            }
        }
        return cm;
    }
}
