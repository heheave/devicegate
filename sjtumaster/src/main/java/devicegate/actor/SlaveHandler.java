package devicegate.actor;

import akka.actor.UntypedActor;
import devicegate.actor.message.*;
import devicegate.launch.SlaveLaunch;
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
        } else {
            log.info("Received Message: " + message);
        }
    }
}
