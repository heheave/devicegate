package devicegate.ctrl;

import akka.actor.ActorRef;
import devicegate.actor.message.CtrlMessage;
import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-6-22.
 */
public class ControlCache {

    private final ActorRef sender;

    private final ActorRef self;

    private final CtrlMessage msg;

    public ControlCache(ActorRef sender, ActorRef self, CtrlMessage msg) {
        this.sender = sender;
        this.self = self;
        this.msg = msg;
    }

    public ActorRef getSender() {
        return sender;
    }

    public ActorRef getSelf() {
        return self;
    }

    public CtrlMessage getMsg() {
        return msg;
    }
}
