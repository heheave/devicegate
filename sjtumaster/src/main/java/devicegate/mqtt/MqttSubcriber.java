package devicegate.mqtt;

import devicegate.conf.Configure;
import devicegate.conf.V;
import devicegate.launch.SlaveLaunch;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.net.InetSocketAddress;
import java.util.UUID;


/**
 * Created by xiaoke on 17-5-18.
 */
public class MqttSubcriber {

    private static final Logger log = Logger.getLogger(MqttSubcriber.class);

    private final Configure conf;

    private final MqttConnectOptions defaultOpt;

    private MqttClient client;

    private final MqttHandler mqttHandler;

    private InetSocketAddress subAddress;

    public MqttSubcriber(SlaveLaunch slaveLaunch, Configure conf) {
        this.conf = conf;
        this.defaultOpt = new MqttConnectOptions();
        this.defaultOpt.setAutomaticReconnect(true);
        this.defaultOpt.setCleanSession(false);
        int mqttConnectTimeout = conf.getIntOrElse(V.MQTT_CONNECT_TIMEOUT, 10000);
        int mqttKeepaliveInterval = conf.getIntOrElse(V.MQTT_KEEPALIVE_INTERVAL, 20000);
        this.defaultOpt.setConnectionTimeout(mqttConnectTimeout);
        this.defaultOpt.setKeepAliveInterval(mqttKeepaliveInterval);
        //this.defaultOpt.setUserName();
        //this.defaultOpt.setPassword();
        this.mqttHandler = new MqttSubcriberHandler(slaveLaunch);
        String brokerAddress = "114.55.92.31";
        //String brokerAddress = conf.getStringOrElse(V.SLAVE_HOST, "114.55.92.31");
        subAddress = new InetSocketAddress(brokerAddress, 1883);

    }


    public void start() throws Exception {

        String mqttServerUrl = String.format("tcp://%s:%d", subAddress.getAddress().getHostAddress(), subAddress.getPort());
        log.info("subsubsub: " + mqttServerUrl);
        final String mqttClientId = conf.getStringOrElse(V.MQTT_CLIENT_ID, UUID.randomUUID().toString());
        try {
            this.client = new MqttClient(mqttServerUrl, mqttClientId, new MemoryPersistence());
        } catch (MqttException e) {
            this.client = null;
            log.info("Inital MQTT client error", e);
            throw e;
        }
        if (!client.isConnected()) {
            client.connect(defaultOpt);
        }
        String mqttSubTopic = conf.getStringOrElse(V.MQTT_SUB_TOPIC, "PLATFORM_MQTT_ACCESS_TOPIC");
        client.setCallback(new MqttCallback() {

            public void connectionLost(Throwable throwable) {
                log.warn("MQTT connect lost: " + throwable);
            }

            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                if (mqttHandler != null) {
                    mqttHandler.messageIn(s, mqttMessage.getPayload());
                } else {
                    log.warn("MQTT handler is not set, message from topic " + s + " will be dropped");
                }
            }

            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                log.warn("MQTT delivery complete");
            }
        });
        client.subscribe(mqttSubTopic);
    }

    public InetSocketAddress getSubcriberAddress() {
        return subAddress;
    }

    public void stop() {
        try {
            this.client.unsubscribe(conf.getStringOrElse(V.MQTT_SUB_TOPIC, "PLATFORM_MQTT_ACCESS_TOPIC"));
            this.client.disconnect();
            this.client.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
