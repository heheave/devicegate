package devicegate.launch;

import devicegate.actor.SlaveActor;
import devicegate.actor.message.MessageFactory;
import devicegate.actor.message.Msg;
import devicegate.actor.message.TellMeMessage;
import devicegate.conf.Configure;
import devicegate.conf.V;
import devicegate.kafka.KafkaSender;
import devicegate.manager.DeviceCacheInfo;
import devicegate.manager.DeviceManager;
import devicegate.mqtt.MqttSubcriber;
import devicegate.netty.NettyServer;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by xiaoke on 17-5-16.
 */
public class SlaveLaunch implements Launch{

    private static final Logger log = Logger.getLogger(SlaveLaunch.class);

    private final AtomicInteger state;

    private final Configure conf;

    private final NettyServer nettyServer;

    private final MqttSubcriber mqttSubcriber;

    private final DeviceManager cm;

    private final SlaveActor slaveActor;

    private final KafkaSender kafkaSender;

    public SlaveLaunch(Configure conf) {
        this.conf = conf;
        this.nettyServer = new NettyServer(this, conf);
        this.mqttSubcriber = new MqttSubcriber(this, conf);
        this.cm = DeviceManager.getInstance();
        this.slaveActor = new SlaveActor(conf, this);
        this.kafkaSender = new KafkaSender(conf);
        this.state = new AtomicInteger(0);
    }

    public void launch() throws Exception{
        if (state.compareAndSet(0, 1)) {
            kafkaSender.start();
            slaveActor.start();
            nettyServer.start();
            mqttSubcriber.start();
            Msg msg = MessageFactory.getMessage(Msg.TYPE.STASLV);
            msg.setAddress(slaveActor.systemAddress());
            //slaveActor.sendToMaster(msg);
            slaveActor.sendToMasterWithReply(msg);
            startHeartbeatThread();
        } else {
            throw new RuntimeException("Failed to launch in state: " + state.get());
        }
    }

    public void shutdown() {
        if (state.compareAndSet(1, 2)) {
            cm.clear();
            Msg msg = MessageFactory.getMessage(Msg.TYPE.STPSLV);
            msg.setAddress(slaveActor.systemAddress());
            boolean retry = false;
            try {
                slaveActor.sendToMasterWithReply(msg);
            } catch (Exception e) {
                retry = true;
                log.info("After stop other parts, we'll retry it");
            }
            nettyServer.stop();
            mqttSubcriber.stop();
            slaveActor.stop();
            kafkaSender.stop();
            if (retry) {
                try {
                    slaveActor.sendToMasterWithReply(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("Retry to sendToMasterWithReply error", e);
                }
            }
        } else {
            throw new RuntimeException("Failed to shutdown in state: " + state.get());
        }
    }

    public int state() {
        return state.get();
    }

    public boolean addChannel(String id, Channel channel) {
        // added to local manager
        DeviceCacheInfo di = new DeviceCacheInfo(channel, conf.getLongOrElse(V.MASTER_SCHELDULE_PERIOD, 5000));
        DeviceCacheInfo oldDi = cm.putIfAbsent(id, di);
        if (oldDi == null) {
            // added to remote manager
            Msg msg = MessageFactory.getMessage(Msg.TYPE.ADDID);
            msg.setId(id);
            msg.setAddress(slaveActor.systemAddress());
            slaveActor.sendToRemote(msg, null);
            // bind id to channel
            if (channel != null) {
                Attribute<String> attr = channel.attr(AttributeKey.<String>valueOf(V.NETTY_CHANNEL_ATTR_KEY));
                if (attr != null) {
                    attr.setIfAbsent(id);
                }
            }
            return true;
        } else {
            oldDi.updateTime();
            return true;
        }
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
            if (cm.remove(id) != null) {
                // remove from remote
                Msg msg = MessageFactory.getMessage(Msg.TYPE.RMID);
                msg.setId(id);
                slaveActor.sendToRemote(msg, null);
            }
            return true;
        } else {
            //cancel id to channel
            Attribute<String> attr = channel.attr(AttributeKey.<String>valueOf(V.NETTY_CHANNEL_ATTR_KEY));
            if (attr != null) {
                String idAttach = attr.get();
                if (idAttach != null) {
                    // remove from local
                    if (cm.remove(idAttach) != null) {
                        // remove from remote
                        Msg msg = MessageFactory.getMessage(Msg.TYPE.RMID);
                        msg.setId(idAttach);
                        slaveActor.sendToRemote(msg, null);
                    }
                }
            }
            return true;
        }
    }


    public void pushToKafka(JSONObject jo) {
        if (!kafkaSender.msgIn(jo)) {
            log.warn("Message: " + jo.toString() + " hasn't push to kafka, because of full queue and 'fullDrop' enabled");
        }
    }

    public void tellToMaster() {
        int maxIds = conf.getIntOrElse(V.ACTOR_TELLME_MAX_IDS, 50);
        List<String> keys = cm.getAllKeys();
        if (keys.isEmpty()) return;
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
        for (List<String> li : res) {
            Msg mes = MessageFactory.getMessage(Msg.TYPE.TELLME);
            mes.setAddress(slaveActor.systemAddress());
            ((TellMeMessage)mes).setIds(li);
            slaveActor.sendToRemote(mes, null);
            li.clear();
        }
        res.clear();
    }

    public void startHeartbeatThread() {
        final long masterPeriod = conf.getLongOrElse(V.MASTER_SCHELDULE_PERIOD, 10000);
        Runnable run = new Runnable() {
            public void run() {
                long lastCleanTime = System.currentTimeMillis();
                while (state() == 1) {
                    long currentTime = System.currentTimeMillis();
                    if (lastCleanTime + masterPeriod < currentTime) {
                        lastCleanTime = currentTime;
                        cm.cleanAll(SlaveLaunch.this, currentTime);
                    }
                    Msg hbMsg = MessageFactory.getMessage(Msg.TYPE.HB);
                    hbMsg.setAddress(slaveActor.systemAddress());
                    long sleepTime = masterPeriod;
                    long timeout = conf.getLongOrElse(V.ACTOR_REPLY_TIMEOUT, 2000);
                    try {
                        slaveActor.sendToMasterWithReply(hbMsg, timeout);
                    } catch (Exception e) {
                        log.info("HeartBeat failed, decrease the time interval");
                        sleepTime >>= 1;
                    }
                    if (sleepTime < timeout) sleepTime = timeout;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        log.info("Slave heartbeat loop sleep error", e);
                    }
                }
            }
        };
        Thread t = new Thread(run);
        t.setDaemon(true);
        t.start();
    }

}
