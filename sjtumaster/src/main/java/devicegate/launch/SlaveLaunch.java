package devicegate.launch;

import devicegate.actor.SlaveActor;
import devicegate.actor.message.AddIdMessage;
import devicegate.actor.message.MessageFactory;
import devicegate.actor.message.Msg;
import devicegate.actor.message.TellMeMessage;
import devicegate.conf.Configure;
import devicegate.conf.JsonField;
import devicegate.conf.V;
import devicegate.ctrl.Controller;
import devicegate.kafka.KafkaSender;
import devicegate.manager.DeviceCacheInfo;
import devicegate.manager.DeviceManager;
import devicegate.mqtt.MqttProxyClient;
import devicegate.netty.SlaveNettyServer;
import devicegate.security.KafkaSendPermission;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by xiaoke on 17-5-16.
 */
public class SlaveLaunch implements Launch{

    private static final Logger log = Logger.getLogger(SlaveLaunch.class);

    private final AtomicInteger state;

    private final Configure conf;

    private final SlaveNettyServer nettyServer;

    private final MqttProxyClient mqttProxyClient;

    private final DeviceManager dm;

    private final SlaveActor slaveActor;

    private final KafkaSender kafkaSender;

    private final Controller controller;

    private final Lock hbLocks;

    private final Condition hbAckCon;

    public SlaveLaunch(Configure conf) {
        this.conf = conf;
        this.nettyServer = new SlaveNettyServer(this, conf);
        this.mqttProxyClient = new MqttProxyClient(this, conf);
        this.dm = DeviceManager.getInstance(this);
        this.slaveActor = new SlaveActor(conf, this);
        this.kafkaSender = new KafkaSender(conf);
        this.controller = new Controller(this, conf);
        this.hbLocks = new ReentrantLock();
        this.hbAckCon = this.hbLocks.newCondition();
        this.state = new AtomicInteger(0);
    }

    public Configure getConf() {
        return conf;
    }

    public DeviceManager getDm() {
        return dm;
    }

    public MqttProxyClient getMqttProxyClient() {
        return mqttProxyClient;
    }

    public Controller getController() {
        return controller;
    }

    public SlaveActor getActor() {
        return slaveActor;
    }

    public KafkaSender getKafkaSender() {
        return kafkaSender;
    }

    public void launch() throws Exception{
        if (state.compareAndSet(0, 1)) {
            kafkaSender.start();
            slaveActor.start();
            nettyServer.start();
            //mqttProxyClient.start();
            controller.start();
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
            dm.clear();
            Msg msg = MessageFactory.getMessage(Msg.TYPE.STPSLV);
            msg.setAddress(slaveActor.systemAddress());
            boolean retry = false;
            if (slaveActor != null) {
                try {
                    slaveActor.sendToMasterWithReply(msg);
                } catch (Exception e) {
                    retry = true;
                    log.info("After stop other parts, we'll retry it");
                }
            }
            controller.stop();
            nettyServer.stop();
            mqttProxyClient.stop();
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


    public void heartbeatAckReceived() {
        hbLocks.lock();
        try {
            hbAckCon.signalAll();
        }finally {
            hbLocks.unlock();
        }
    }

    public void startHeartbeatThread() {
        final long masterPeriod = conf.getLongOrElse(V.MASTER_SCHELDULE_PERIOD, 10000);
        Runnable run = new Runnable() {
            public void run() {
                long lastCleanTime = System.currentTimeMillis();
                long lastSleepTime = masterPeriod;
                try {
                    Thread.sleep(masterPeriod);
                } catch (InterruptedException e) {
                    log.warn("slave heartbeat sleep for delay error", e);
                }
                while (state() == 1) {
                    long currentTime = System.currentTimeMillis();
                    if (lastCleanTime + masterPeriod < currentTime) {
                        lastCleanTime = currentTime;
                        dm.cleanAll(currentTime);
                    }
                    Msg hbMsg = MessageFactory.getMessage(Msg.TYPE.HB);
                    hbMsg.setAddress(slaveActor.systemAddress());
                    long sleepTime = lastSleepTime;
                    long timeout = conf.getLongOrElse(V.ACTOR_REPLY_TIMEOUT, 2000);
                    slaveActor.sendToRemote(hbMsg, null);
                    //slaveActor.sendToMasterWithReply(hbMsg, timeout);
                    hbLocks.lock();
                    try {
                        if (!hbAckCon.await(timeout, TimeUnit.MILLISECONDS)) {
                            log.info("HeartBeat failed, decrease the time interval");
                            sleepTime >>= 1;
                        } else {
                            log.info("HeartBeat succeed, reset the time interval");
                            sleepTime = masterPeriod;
                        }
                    } catch (Exception e) {
                        log.info("HeartBeat failed, decrease the time interval");
                        sleepTime >>= 1;
                    } finally {
                        hbLocks.unlock();
                    }
                    if (sleepTime < timeout) sleepTime = timeout;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        log.info("Slave heartbeat loop sleep error", e);
                    } finally {
                        lastSleepTime = sleepTime;
                    }
                }
            }
        };
        Thread t = new Thread(run);
        t.setDaemon(true);
        t.start();
    }
}
