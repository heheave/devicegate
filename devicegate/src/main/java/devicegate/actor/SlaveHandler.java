package devicegate.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import devicegate.actor.message.*;
import devicegate.conf.JsonField;
import devicegate.launch.SlaveLaunch;
import devicegate.manager.DeviceCacheInfo;
import devicegate.security.DeviceCtrlPermission;
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
            tellToMaster();
        } else if (message instanceof CtrlMessage){
            ctrl((CtrlMessage)message);
        } else {
            log.info("Received Message: " + message);
        }
    }

    private void ctrl(CtrlMessage ctrlMessage) {
        boolean isIn = slaveLaunch.getController().ctrlMsgIn(getSender(), getSelf(), ctrlMessage);
        if (!isIn) {
            String ackInfo = "Ctrl thread is busy";
            if (ctrlMessage.isRet()) {
                Msg ackMsg = MessageFactory.getMessage(Msg.TYPE.ACK);
                if (ackInfo != null) {
                    ((AckMessage)ackMsg).setAckInfo(ackInfo);
                }
                getSender().tell(ackMsg, getSelf());
            }
        }
    }

    private void tellToMaster() {
        List<List<String>> res = slaveLaunch.getDm().getTellMasterToInfo();
        try {
            for (List<String> li : res) {
                Msg mes = MessageFactory.getMessage(Msg.TYPE.TELLME);
                mes.setAddress(slaveLaunch.getActor().systemAddress());
                ((TellMeMessage) mes).setTellInfo(li);
                slaveLaunch.getActor().sendToRemote(mes, null);
                li.clear();
            }
        } finally {
            res.clear();
        }
    }
}
