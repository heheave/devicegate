package devicegate.manager;


import devicegate.actor.message.AddIdMessage;
import devicegate.actor.message.MessageFactory;
import devicegate.actor.message.Msg;
import devicegate.conf.V;
import devicegate.launch.SlaveLaunch;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by xiaoke on 17-5-16.
 */
public class DeviceManager extends AbstactManager<String, DeviceCacheInfo>{

    private static final Logger log = Logger.getLogger(DeviceManager.class);

    private static DeviceManager cm = null;

    private final SlaveLaunch slaveLaunch;

    private final AtomicInteger msgNum = new AtomicInteger(0);

    private final AtomicLong msgBytes = new AtomicLong(0L);

    private volatile long dayTime = System.currentTimeMillis() / 86400000;

    private final AtomicInteger msgNumT = new AtomicInteger(0);

    private final AtomicLong msgBytesT = new AtomicLong(0L);

    private DeviceManager(SlaveLaunch slaveLaunch) {
        this.slaveLaunch = slaveLaunch;
    }

    @Override
    void afterRemoved(DeviceCacheInfo oldValue) {
        Channel channel;
        if (oldValue != null && (channel = oldValue.getChannel()) != null) {
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
                    ptc = DeviceCacheInfo.Protocol.TCP.name();
                } else {
                    ptc = DeviceCacheInfo.Protocol.MQTT.name();
                }
                keysCopy.add(String.format("%s,%s", entry.getValue(), ptc));
            }
        }
        return keysCopy;
    }

    public void cleanAll(long time) {
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
            removeChannel(key);
        }
        toRemoveKey.clear();
    }

    public static DeviceManager getInstance(SlaveLaunch slaveLaunch) {
        if (cm == null) {
            synchronized (DeviceManager.class) {
                if (cm == null) {
                    assert slaveLaunch != null: "Slave should not bee null at first getInstance";
                    cm = new DeviceManager(slaveLaunch);
                }
            }
        }
        return cm;
    }

    public DeviceCacheInfo addChannel(String id, Channel channel) {
        // added to local manager

        DeviceCacheInfo di = new DeviceCacheInfo(id, channel, slaveLaunch.getConf().getLongOrElse(V.SLAVE_SESSION_TIMEOUT));
        put(id, di);
        // added to remote manager
        AddIdMessage msg = (AddIdMessage) MessageFactory.getMessage(Msg.TYPE.ADDID);
        msg.setId(id);
        msg.setProtocol(di.protocol().name());
        msg.setAddress(slaveLaunch.getActor().systemAddress());
        slaveLaunch.getActor().sendToRemote(msg, null);
        // bind id to channel
        if (channel != null) {
            Attribute<String> attr = channel.attr(AttributeKey.<String>valueOf(V.NETTY_CHANNEL_ATTR_KEY));
            if (attr != null) {
                attr.setIfAbsent(id);
            }
        }
        return di;
    }

    public boolean removeChannel(String id) {
        return removeChannel(id, null);
    }

    public boolean removeChannel(Channel channel) {
        return removeChannel(null, channel);
    }

    private boolean removeChannel(String id, Channel channel) {
        if (id == null && channel == null) {
            return false;
        } else if (channel == null) {
            if (remove(id) != null) {
                // remove from remote
                Msg msg = MessageFactory.getMessage(Msg.TYPE.RMID);
                msg.setId(id);
                msg.setAddress(slaveLaunch.getActor().systemAddress());
                slaveLaunch.getActor().sendToRemote(msg, null);
            }
            return true;
        } else {
            //cancel id to channel
            Attribute<String> attr = channel.attr(AttributeKey.<String>valueOf(V.NETTY_CHANNEL_ATTR_KEY));
            if (attr != null) {
                String idAttach = attr.get();
                if (idAttach != null) {
                    // remove from local
                    if (remove(idAttach) != null) {
                        // remove from remote
                        Msg msg = MessageFactory.getMessage(Msg.TYPE.RMID);
                        msg.setId(idAttach);
                        msg.setAddress(slaveLaunch.getActor().systemAddress());
                        slaveLaunch.getActor().sendToRemote(msg, null);
                    }
                }
            }
            return true;
        }
    }

    public List<List<String>> getTellMasterToInfo() {
        int maxIds = slaveLaunch.getConf().getIntOrElse(V.ACTOR_TELLME_MAX_IDS);
        List<String> keys = getAllKeys();
        if (keys.isEmpty()) return null;
        int keySize = keys.size();
        int resListNum = keySize % maxIds == 0 ? keySize / maxIds : keySize / maxIds + 1;
        List<List<String>> res = new ArrayList<List<String>>(resListNum);
        int full = keySize / resListNum + 1;
        int cnt = 1;
        List<String> tmp = new ArrayList<String>(full);
        for (String key: keys) {
            if (cnt == full) {
                res.add(tmp);
                tmp = new ArrayList<String>(full);
            } else {
                tmp.add(key);
            }
            cnt++;
        }
        if (!tmp.isEmpty()){
            res.add(tmp);
        }
        return res;
    }

    public void msgStatisticUp(long time, int num, long bytes) {
        long tmpDt = time / 86400000;
        if (tmpDt != dayTime) {
            dayTime = tmpDt;
            msgNumT.set(0);
            msgBytesT.set(0L);
        }
        msgNum.addAndGet(num);
        msgBytes.addAndGet(bytes);
        msgNumT.addAndGet(num);
        msgBytesT.addAndGet(bytes);
    }

    public int getMsgNum() {
        return msgNum.get();
    }

    public long getMsgBytes() {
        return msgBytes.get();
    }

    public int getMsgNumT() {
        return msgNumT.get();
    }

    public long getMsgBytesT() {
        return msgBytesT.get();
    }

    public int getConnectionSize() {
        return idToCacheObj.size();
    }
}
