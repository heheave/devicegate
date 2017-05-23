package devicegate.mqtt;

import devicegate.conf.Configure;
import devicegate.conf.V;
import devicegate.launch.SlaveLaunch;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by xiaoke on 17-5-18.
 */
public class MqttSubscriber {

    private static final Logger log = Logger.getLogger(MqttSubscriber.class);

    private final Configure conf;

    private final MqttConnectOptions defaultOpt;

    private MqttClient client;

    private final MqttHandler mqttHandler;

    private InetSocketAddress subAddress;

    private final String subTopic;

    private volatile boolean isRec;

    public MqttSubscriber(SlaveLaunch slaveLaunch, Configure conf) {
        this.conf = conf;
        this.defaultOpt = new MqttConnectOptions();
        this.defaultOpt.setAutomaticReconnect(false);
        this.defaultOpt.setCleanSession(false);
        int mqttConnectTimeout = conf.getIntOrElse(V.MQTT_CONNECT_TIMEOUT, 1000);
        int mqttKeepaliveInterval = conf.getIntOrElse(V.MQTT_KEEPALIVE_INTERVAL, 20000);
        this.defaultOpt.setConnectionTimeout(mqttConnectTimeout);
        this.defaultOpt.setKeepAliveInterval(mqttKeepaliveInterval);
        //this.defaultOpt.setUserName();
        //this.defaultOpt.setPassword();
        this.subTopic = conf.getStringOrElse(V.MQTT_SUB_TOPIC, "PLATFORM_MQTT_ACCESS_TOPIC");
        this.mqttHandler = new MqttSubscriberHandler(slaveLaunch);
        String brokerAddress = "114.55.92.31";
        //String brokerAddress = conf.getStringOrElse(V.SLAVE_HOST, "114.55.92.31");
        this.subAddress = new InetSocketAddress(brokerAddress, 1883);
        this.isRec = false;
    }


    public void start() throws Exception {
        String mqttServerUrl = String.format("tcp://%s:%d", subAddress.getAddress().getHostAddress(), subAddress.getPort());
        log.info("subsubsub: " + mqttServerUrl);
        final String mqttClientId = conf.getStringOrElse(V.MQTT_CLIENT_ID, UUID.randomUUID().toString());
        try {
            client = new MqttClient(mqttServerUrl, mqttClientId, new MemoryPersistence());
        } catch (MqttException e) {
            client = null;
            log.info("Inital MQTT client error", e);
            throw e;
        }
        if (!client.isConnected()) {
            client.connect(defaultOpt);
        }

        client.setCallback(new MqttCallback() {

            public void connectionLost(Throwable throwable) {
                log.warn("MQTT connect lost start reconnect runner: " + throwable);
                startReconnetRunner();
            }

            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                log.info(s);
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
        log.info(subTopic);
        client.subscribe(subTopic);
    }

    private void startReconnetRunner() {
        if (isRec) {
            return;
        }
        final long sleepIfFailed = conf.getLongOrElse(V.MQTT_RECONNECT_PERIOD, 5000);
        Runnable run = new Runnable() {
            public void run() {
                // 0 is not connect
                // 1 is not subscribe
                // 2 is success
                isRec = true;
                int recState = client.isConnected() ? 1 : 0;
                while (recState != 2) {
                    if (recState == 0) {
                        try {
                            client.connect(defaultOpt);
                            recState = 1;
                        } catch (MqttException e) {
                            recState = 0;
                        }
                    }

                    if (recState == 1) {
                        try {
                            client.subscribe(subTopic);
                            recState = 2;
                        } catch (MqttException e) {
                            recState = client.isConnected() ? 1 : 0;
                        }
                    }

                    if (recState != 2) {
                        try {
                            Thread.sleep(sleepIfFailed);
                        } catch (InterruptedException e) {
                            log.info("Reconnect sleep error", e);
                        }
                    }
                }
                isRec = false;
            }
        };
        Thread t = new Thread(run);
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        try {
            if (!isRec) {
                log.info("MQTT stop first unsubscribe");
                this.client.unsubscribe(subTopic);
            }
            log.info("MQTT stop second disconnect and close");
            client.disconnect();
            client.close();
            log.info("MQTT subscriber has been stopped");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public InetSocketAddress getSubcriberAddress() {
        return subAddress;
    }
}
