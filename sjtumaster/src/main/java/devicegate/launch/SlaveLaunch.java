package devicegate.launch;

import devicegate.actor.SlaveActor;
import devicegate.actor.message.MessageFactory;
import devicegate.actor.message.Msg;
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
        if (cm.putIfAbsent(id, di) == null) {
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

    public InetSocketAddress getActorSystemAddress() {
        return slaveActor.systemAddress();
    }

    public void pushToKafka(JSONObject jo) {
        if (!kafkaSender.msgIn(jo)) {
            log.warn("Message: " + jo.toString() + " hasn't push to kafka, because of full queue and 'fullDrop' enabled");
        }
    }

    public void cleanAll() {
        cm.cleanAll(this, System.currentTimeMillis());
    }

}
