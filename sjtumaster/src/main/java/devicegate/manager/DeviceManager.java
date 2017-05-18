package devicegate.manager;


import devicegate.launch.SlaveLaunch;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoke on 17-5-16.
 */
public class DeviceManager extends AbstactManager<DeviceCacheInfo>{

    private static final Logger log = Logger.getLogger(DeviceManager.class);

    private static DeviceManager cm = null;

    @Override
    void afterRemoved(DeviceCacheInfo oldValue) {
        if (oldValue != null && oldValue.getChannel() != null) {
            Channel channel =  oldValue.getChannel();
            channel.close();
        }
    }

    public void cleanAll(SlaveLaunch slaveLaunch, long time) {
        synchronized (idToCacheObj) {
            List<String> toRemoveKey = new LinkedList<String>();
            for (Map.Entry<String, DeviceCacheInfo> entry: idToCacheObj.entrySet()) {
                if (entry.getValue().isExpired(time)) {
                    toRemoveKey.add(entry.getKey());
                }
            }

            for (String key: toRemoveKey) {
                slaveLaunch.removeChannel(key);
            }
        }
    }

    public static DeviceManager getInstance() {
        if (cm == null) {
            synchronized (DeviceManager.class) {
                if (cm == null) {
                    cm = new DeviceManager();
                }
            }
        }
        return cm;
    }
}
