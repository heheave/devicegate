package devicegate.actor;

import akka.actor.UntypedActor;
import devicegate.actor.message.*;
import devicegate.launch.MasterLaunch;
import devicegate.manager.MachineManager;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by xiaoke on 17-5-16.
 */
public class MasterHandler extends UntypedActor {

    private static final Logger log = Logger.getLogger(MasterHandler.class);

    private final MasterActor masterActor;

    public MasterHandler(MasterActor masterActor) {
        this.masterActor = masterActor;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("Receive message: " + ((Msg)message).type());
        if (message instanceof StartSlvMessage) {
            StartSlvMessage startSlvMessage = (StartSlvMessage) message;
            startSlv(startSlvMessage);
            getSender().tell(MessageFactory.getMessage(Msg.TYPE.ACK), getSelf());
        } else if (message instanceof StopSlvMessage) {
            StopSlvMessage stopSlvMessage = (StopSlvMessage) message;
            stopSlv(stopSlvMessage);
            getSender().tell(MessageFactory.getMessage(Msg.TYPE.ACK), getSelf());
        } else if (message instanceof AddIdMessage) {
            AddIdMessage addIdMessage = (AddIdMessage)message;
            addId(addIdMessage);
        } else if(message instanceof RmIdMessage) {
            RmIdMessage rmIdMessage = (RmIdMessage)message;
            log.info("Removed: " + rmIdMessage.getId());
            rmId(rmIdMessage);
        } else if (message instanceof HBMessage) {
            HBMessage hbMessage = (HBMessage)message;
            getSender().tell(MessageFactory.getMessage(Msg.TYPE.ACK), getSelf());
            InetSocketAddress shouldTellMeAddr = hb(hbMessage);
            if (shouldTellMeAddr != null) {
                masterActor.sendToRemote(MessageFactory.getMessage(Msg.TYPE.TELLME), shouldTellMeAddr);
            }
        } else if(message instanceof TellMeMessage) {
            TellMeMessage tellMeMessage = (TellMeMessage)message;
            tellMe(tellMeMessage);
        } else {
            log.info("Received message: " + message);
        }
    }

    private void startSlv(StartSlvMessage startSlvMessage) {
        InetSocketAddress isa = startSlvMessage.getAddress();
        if (isa != null) {
            log.info(isa.getAddress().getHostAddress() + ":" + isa.getPort() + " connected to master");
        }
        MachineManager.getInstance().updateAddress(isa);
    }

    private void stopSlv(StopSlvMessage stopSlvMessage) {
        InetSocketAddress isa = stopSlvMessage.getAddress();
        if (isa != null) {
            log.info(isa.getAddress().getHostAddress() + ":" + isa.getPort() + " disconnected to master");
        }
        MachineManager.getInstance().removeAddress(isa);
    }

    private void addId(AddIdMessage addIdMessage) {
        String id = addIdMessage.getId();
        InetSocketAddress isa = addIdMessage.getAddress();
        if (id != null) {
            MachineManager.getInstance().update(id, isa, addIdMessage.getProtocol());
        }
    }

    private void rmId(RmIdMessage rmIdMessage) {
        String id = rmIdMessage.getId();
        InetSocketAddress isa = rmIdMessage.getAddress();
        if (id != null) {
            if (isa != null) {
                MachineManager.getInstance().remove(id, isa);
            } else {
                MachineManager.getInstance().remove(id);
            }
        }
    }

    private InetSocketAddress hb(HBMessage hbMessage) {
        InetSocketAddress isa = hbMessage.getAddress();
        if (isa != null) {
            if (!MachineManager.getInstance().updateAddress(isa)) {
                return isa;
            }
        }
        return null;
    }

    private void tellMe(TellMeMessage tellMeMessage) {
        InetSocketAddress isa = tellMeMessage.getAddress();
        if (isa != null) {
            List<String> infos = tellMeMessage.getTellInfo();
            if (infos != null) {
                for (String info : infos) {
                    String[] i = info.split(",", 2);
                    MachineManager.getInstance().update(i[0], isa, i[1]);
                }
            }
        }
    }
}
