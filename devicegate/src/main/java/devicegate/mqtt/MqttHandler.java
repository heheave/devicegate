package devicegate.mqtt;

/**
 * Created by xiaoke on 17-5-18.
 */
public interface MqttHandler {

    void messageIn(MqttProxyClient client, String topic, byte[] bytes);

}
