package devicegate.mqtt;

import devicegate.protocol.MessageHandler;

/**
 * Created by xiaoke on 17-5-18.
 */
public interface MqttHandler extends MessageHandler{

    void messageIn(MqttProxyServer client, String topic, byte[] bytes);

}
