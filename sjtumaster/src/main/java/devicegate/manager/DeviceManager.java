package devicegate.manager;


import devicegate.conf.V;
import devicegate.launch.SlaveLaunch;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.log4j.Logger;

import java.util.*;

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
            Attribute<String> attr = channel.attr(AttributeKey.<String>valueOf(V.NETTY_CHANNEL_ATTR_KEY));
            if (attr != null) {
                attr.setIfAbsent(null);
            }
            channel.close();
        }
    }

    public List<String> getAllKeys() {
        List<String> keysCopy = new LinkedList<String>();
        for (Map.Entry<String, DeviceCacheInfo> entry: idToCacheObj.entrySet()) {
            if (entry != null) {
                String ptc;
                if (entry.getValue().getChannel() != null) {
                    ptc = V.TCP;
                } else {
                    ptc = V.MQTT;
                }
                keysCopy.add(String.format("%s,%s", entry.getKey(), ptc));
            }
        }
        return keysCopy;
    }

    public void cleanAll(SlaveLaunch slaveLaunch, long time) {
        // ConcurrentHashMap doesn't neet synchronized
        // however entry.value may be null under concurrency condition
        List<String> toRemoveKey = new LinkedList<String>();
        for (Map.Entry<String, DeviceCacheInfo> entry: idToCacheObj.entrySet()) {
            if (entry.getValue() != null && entry.getValue().isExpired(time)) {
                toRemoveKey.add(entry.getKey());
            }
        }

        for (String key: toRemoveKey) {
            log.info("Close channel " + key + " because of timeout");
            slaveLaunch.removeChannel(key);
        }
        toRemoveKey.clear();
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
