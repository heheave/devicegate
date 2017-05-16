package devicegate.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.remote.RemoteActorRef;

/**
 * Created by xiaoke on 17-5-16.
 */
public class SlaveHandler extends UntypedActor {

    @Override
    public void onReceive(Object message) throws Exception {
        System.out.println("receive msg: " + message);
    }
}
