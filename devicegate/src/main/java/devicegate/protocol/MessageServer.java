package devicegate.protocol;

import devicegate.conf.Configure;
import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-8-19.
 */

/**
 * MessageServer act as a message server and wait for a message in
 * then call back the MessageHandler set, also it can actively send
 * a message to device specified.
 */
abstract public class MessageServer {

    protected final Configure conf;

    public MessageServer(Configure conf) {
        this.conf = conf;
    }

    abstract public void start() throws Exception;

    abstract public void stop() throws Exception;

    abstract public void setMessageHandler(MessageHandler mi);

    abstract public void sendMessage(JSONObject jo, AttachInfo attachInfo) throws MessageException;
}
