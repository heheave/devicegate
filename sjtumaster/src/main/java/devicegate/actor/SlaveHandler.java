package devicegate.actor;

import akka.actor.UntypedActor;
import devicegate.actor.message.*;
import devicegate.conf.JsonField;
import devicegate.launch.SlaveLaunch;
import devicegate.manager.DeviceCacheInfo;
import net.sf.json.JSONObject;
import org.apache.commons.collections.Factory;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by xiaoke on 17-5-16.
 */
public class SlaveHandler extends UntypedActor {

    private static final Logger log = Logger.getLogger(SlaveHandler.class);

    private SlaveLaunch slaveLaunch;

    private InetSocketAddress systemAddress;

    public SlaveHandler(SlaveLaunch slaveLaunch, InetSocketAddress systemAddress) {
        this.slaveLaunch = slaveLaunch;
        this.systemAddress = systemAddress;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("Received Message: " + ((Msg)message).type());
        if (message instanceof AckMessage) {
            slaveLaunch.heartbeatAckReceived();
        } else if (message instanceof TellMeMessage) {
            slaveLaunch.tellToMaster();
        } else if (message instanceof CtrlMessage){
            CtrlMessage ctrlMessage = (CtrlMessage)message;
            String ackInfo = ctrl(ctrlMessage);
            if (ctrlMessage.isRet()) {
                Msg ackMsg = MessageFactory.getMessage(Msg.TYPE.ACK);
                if (ackInfo != null) {
                    ((AckMessage)ackMsg).setAckInfo(ackInfo);
                }
                getSender().tell(ackMsg, getSelf());
            }
        } else {
            log.info("Received Message: " + message);
        }
    }

    private String ctrl(CtrlMessage ctrlMessage) {
        JSONObject data = ctrlMessage.data();
        String did = data != null ? data.getString(JsonField.DeviceCtrl.ID) : null;
        if (did != null) {
            DeviceCacheInfo dci = slaveLaunch.getDm().get(did);
            if (dci != null) {
                if (DeviceCacheInfo.Protocol.TCP.equals(dci.protocol())) {
                    try {
                        dci.getChannel().writeAndFlush(data.toString()).sync();
                        return null;
                    } catch (InterruptedException e) {
                        log.warn("TCP writeAndFlush error", e);
                        return "TCP writeAndFlush error";
                    }
                } else if (DeviceCacheInfo.Protocol.MQTT.equals(dci.protocol())){
                    if (slaveLaunch.getMqttProxyClient().pub(String.format("device_ctrl_%s", did), data.toString())) {
                        return null;
                    } else {
                        return "MQTT pub error";
                    }
                } else {
                    return "Unsupported protocol " + dci.protocol().name();
                }
            } else {
                log.warn("Ctrl device id is " + did + ", but this device is not connected");
                return "Ctrl device id is " + did + ", but this device is not connected";
            }
        } else {
            log.warn("Ctrl device id is null or data is null");
            return "Ctrl device id is null or data is null";
        }
    }
}
