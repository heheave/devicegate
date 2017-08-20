package devicegate.netty;

import devicegate.conf.Configure;
import devicegate.launch.SlaveLaunch;
import devicegate.netty.handler.SlaveMessageHandler;
import devicegate.protocol.*;
import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-8-19.
 */
public class TcpProtocolManager extends ProtocolManager{

    public TcpProtocolManager(MessageServer messageServer, SlaveLaunch slaveLaunch, Configure conf) {
        super(messageServer, slaveLaunch, conf);
    }

    @Override
    public void initial() {
        MessageHandler messageHandler = new SlaveMessageHandler(this);
        messageServer.setMessageHandler(messageHandler);
        super.initial();
    }

    @Override
    public AuthRet authorize(JSONObject jo, AuthType type) {
        // TODO(xiaoke): Now we don't do authorizing
        return AuthRet.AUTH_PASSED;
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.TCP;
    }

    @Override
    public void mesErrorTackle(MessageException e) {
        // TODO(xiaoke): Now we don't do error tackling
    }
}
