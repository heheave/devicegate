package devicegate.ctrl;

import akka.actor.ActorRef;
import devicegate.actor.message.AckMessage;
import devicegate.actor.message.CtrlMessage;
import devicegate.actor.message.MessageFactory;
import devicegate.actor.message.Msg;
import devicegate.conf.Configure;
import devicegate.conf.JsonField;
import devicegate.conf.V;
import devicegate.launch.SlaveLaunch;
import devicegate.manager.DeviceCacheInfo;
import devicegate.security.DeviceCtrlPermission;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by xiaoke on 17-6-22.
 */
public class Controller {

    private static final Logger log = Logger.getLogger(Controller.class);

    private volatile boolean isAlive;

    private final BlockingQueue<ControlCache> cacheQueue;

    private final SlaveLaunch slaveLaunch;

    private final Configure conf;

    public Controller(SlaveLaunch slaveLaunch, Configure conf) {
        this.slaveLaunch = slaveLaunch;
        this.conf = conf;
        int maxSize = conf.getIntOrElse(V.CTRL_QUEUE_COMPACITY, 1000);
        this.cacheQueue = new ArrayBlockingQueue<ControlCache>(maxSize);
    }

    public void start() {
        this.isAlive = true;
        startCtrlRunner();
        log.info("Controller has been started");
    }

    private void startCtrlRunner() {
        Thread t  = new Thread() {
            public void run() {
                while (isAlive) {
                    try {
                        ControlCache cc = cacheQueue.take();
                        //Thread.sleep(5000);
                        ctrlOnce(cc);
                    } catch (InterruptedException e) {
                        log.warn("Take from CacheQueue error", e);
                    } catch (Exception e) {
                        log.warn("Ctrl error", e);
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private void ctrlOnce(ControlCache cc) {
        if (cc != null && cc.getSender() != null && cc.getMsg() != null) {
            JSONObject data = cc.getMsg().data();
            String did = data != null ? data.getString(JsonField.DeviceCtrl.ID) : null;
            if (did != null) {
                DeviceCacheInfo dci = slaveLaunch.getDm().get(did);
                if (dci != null) {
                    try {
                        String magic = data.containsKey(JsonField.DeviceCtrl.MAGIC) ?
                                data.getString(JsonField.DeviceCtrl.MAGIC) : null;
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) {
                            sm.checkPermission(new DeviceCtrlPermission(magic));
                        }
                        if (DeviceCacheInfo.Protocol.TCP.equals(dci.protocol())) {

                            dci.getChannel().writeAndFlush(data.toString()).sync();
                            tellTo(cc, null);

                        } else if (DeviceCacheInfo.Protocol.MQTT.equals(dci.protocol())){
                            if (slaveLaunch.getMqttProxyClient().pub(String.format("device_ctrl_%s", did), data.toString())) {
                                tellTo(cc, null);
                            } else {
                                tellTo(cc, "MQTT pub error");
                            }
                        } else {
                            tellTo(cc, "Unsupported protocol " + dci.protocol().name());
                        }
                    } catch (Exception e) {
                        log.warn("Device ctrl error", e);
                        tellTo(cc, "Device ctrl error: " + e.getMessage());
                    }
                } else {
                    log.warn("Ctrl device id is " + did + ", but this device is not connected");
                    tellTo(cc, "Ctrl device id is " + did + ", but this device is not connected");
                }
            } else {
                log.warn("Ctrl device id is null or data is null");
                tellTo(cc, "Ctrl device id is null or data is null");
            }
        }
    }

    private void tellTo(ControlCache cc, String ackInfo) {
        CtrlMessage ctrlMessage = cc.getMsg();
        if (ctrlMessage.isRet()) {
            Msg ackMsg = MessageFactory.getMessage(Msg.TYPE.ACK);
            if (ackInfo != null) {
                ((AckMessage)ackMsg).setAckInfo(ackInfo);
            }
            if (cc.getSender() != null) {
                cc.getSender().tell(ackMsg, cc.getSelf());
            }
        }
    }

    public boolean ctrlMsgIn(ActorRef sender, ActorRef self, CtrlMessage ctrlMessage) {
        ControlCache cc = new ControlCache(sender, self, ctrlMessage);
        if (cacheQueue.isEmpty()) {
            ctrlOnce(cc);
            return true;
        } else {
            return cacheQueue.offer(cc);
        }
    }

    public void stop() {
        this.isAlive = false;
        log.info("Controller has been stopped");
    }
}
