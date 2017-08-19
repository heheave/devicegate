package devicegate.mqtt;

import devicegate.conf.Configure;
import devicegate.launch.SlaveLaunch;
import devicegate.protocol.*;
import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-8-19.
 */
public class MqttProtocolManager extends ProtocolManager{

    public MqttProtocolManager(MessageServer messageServer, SlaveLaunch slaveLaunch, Configure conf) {
        super(messageServer, slaveLaunch, conf);
    }

    @Override
    public void initial() {
        MessageHandler messageHandler = new MqttMessageHandler(this);
        messageServer.setMessageHandler(messageHandler);
        super.initial();
    }

    @Override
    public AuthRet authorize(JSONObject jo, AuthType type) {
        // TODO(xiaoke): Now we don't do authorizing
        return AuthRet.apply(true, null);
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.MQTT;
    }

    @Override
    public void mesErrorTackle(MessageException e) {
        // TODO(xiaoke): Now we don't do authorizing
    }
}
