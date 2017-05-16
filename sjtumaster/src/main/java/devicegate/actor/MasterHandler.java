package devicegate.actor;

import akka.actor.UntypedActor;
import devicegate.actor.message.AddIdMessage;
import devicegate.actor.message.RmIdMessage;
import org.apache.log4j.Logger;

/**
 * Created by xiaoke on 17-5-16.
 */
public class MasterHandler extends UntypedActor {

    private static final Logger log = Logger.getLogger(MasterHandler.class);

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof AddIdMessage) {
            AddIdMessage addIdMessage = (AddIdMessage)message;
            addId(addIdMessage);
        } else if(message instanceof RmIdMessage) {
            RmIdMessage rmIdMessage = (RmIdMessage)message;
            rmId(rmIdMessage);
        } else {
            log.info("Received message: " + message);
        }
    }

    private void addId(AddIdMessage addIdMessage) {

    }

    private void rmId(RmIdMessage rmIdMessage) {

    }
}
