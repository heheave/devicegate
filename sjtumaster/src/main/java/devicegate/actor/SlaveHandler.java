package devicegate.actor;

import akka.actor.UntypedActor;
import devicegate.actor.message.HBMessage;
import devicegate.actor.message.MessageFactory;
import devicegate.actor.message.Msg;
import devicegate.launch.SlaveLaunch;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;

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
        System.out.println("Received Message: " + message.getClass().getName());
        if (message instanceof HBMessage) {
            Msg mes = MessageFactory.getMessage(Msg.TYPE.HB);
            mes.setAddress(systemAddress);
            getSender().tell(mes, getSelf());
            slaveLaunch.cleanAll();
        } else {
            log.info("Received Message: " + message);
        }
    }
}
