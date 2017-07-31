package devicegate.kafka;

import devicegate.actor.message.MessageFactory;
import devicegate.actor.message.Msg;
import devicegate.conf.Configure;
import devicegate.conf.JsonField;
import devicegate.conf.V;
import devicegate.launch.MasterLaunch;
import devicegate.manager.MachineCacheInfo;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Created by xiaoke on 17-6-19.
 */
public class KafkaReceiver {

    private static final Logger log = Logger.getLogger(KafkaReceiver.class);

    private final Configure conf;

    private final MasterLaunch master;

    private final ExecutorService executors;

    private final String kafkaCtrlTopic;

    private ConsumerConnector connector;

    public KafkaReceiver(MasterLaunch master) {
        super();
        this.master = master;
        this.conf = master.getConf();
        this.kafkaCtrlTopic = conf.getStringOrElse(V.KAFKA_CTRL_TOPIC, "device-ctrl-topic");
        this.executors = Executors.newSingleThreadExecutor();
    }

    public void start() throws Exception {
        ConsumerConfig config = createConsumerConfig();
        connector = Consumer.createJavaConsumerConnector(config);

        Map<String, Integer> topicThreadsMap = new HashMap<String, Integer>();
        topicThreadsMap.put(kafkaCtrlTopic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> kafkaStreams =
                connector.createMessageStreams(topicThreadsMap);
        List<KafkaStream<byte[], byte[]>> topicStreams = kafkaStreams.get(kafkaCtrlTopic);
        for (final KafkaStream<byte[], byte[]> stream: topicStreams) {
            ctrlTaskSubmit(stream);
        }
    }

    private void ctrlTaskSubmit(final KafkaStream<byte[], byte[]> stream) {
        executors.submit(new Runnable() {
            public void run() {
                ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
                while (iterator.hasNext()) {
                    MessageAndMetadata<byte[], byte[]> mam = iterator.next();
                    //String topic = mam.topic();
                    //String key = mam.key() != null ? new String(mam.key()) : null;
                    String message = mam.message() != null ? new String(mam.message()) : null;
                    //int partition = mam.partition();
                    JSONObject jo = null;
                    try {
                        jo = JSONObject.fromObject(message);
                    } catch (Exception e) {
                        log.warn(String.format("Transfer %s to JSONObject error", message), e);
                    }
                    tackleCtrl(jo);
                }
            }
        });
    }

    private void tackleCtrl(JSONObject jo) {
        if (jo != null) {
            String id = jo.containsKey(JsonField.DeviceCtrl.ID) ?
                    jo.getString(JsonField.DeviceCtrl.ID) : null;
            if (id != null) {
                MachineCacheInfo mci = master.getMm().get(id);
                if (mci != null && mci.getIsa() != null) {
                    master.getMasterActor().sendToRemote(
                            MessageFactory.getMessage(Msg.TYPE.CTRL, jo), mci.getIsa());
                }
            }
        }
    }

    private ConsumerConfig createConsumerConfig() {
        String zookeeper = conf.getStringOrElse(V.KAFKA_ZK_LIST, "192.168.1.110:2181");
        String broker = conf.getStringOrElse(V.KAFKA_BROKER_LIST, "192.168.1.110:9092");
        String groupId = conf.getStringOrElse(V.KAFKA_GROUP_ID, "CTRL_CONSUMER_GROUP");
        String autoOffsetReset = conf.getStringOrElse(V.KAFKA_AUTO_OFFSET_RESET, "largest");
        String sessionTimeout = conf.getStringOrElse(V.KAFKA_ZK_SESSION_TIMEOUT, "7000");
        String rbMaxRetries = conf.getStringOrElse(V.KAFKA_REBALANCE_MAX_RETRIES, "4");
        String rbBackOff = conf.getStringOrElse(V.KAFKA_REBALANCE_BACKOFF, "2000");
        String syncTime = conf.getStringOrElse(V.KAFKA_ZK_SYNC_TIME, "2000");
        String autoCommit = conf.getStringOrElse(V.KAFKA_AUTO_COMMIT_INTERVAL, "1000");
        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeper);
        props.put("metadata.broker.list", broker);
        props.put("group.id", groupId);
        props.put("auto.offset.reset", autoOffsetReset);
        props.put("zookeeper.session.timeout.ms", sessionTimeout);
        props.put("rebalance.max.retries", rbMaxRetries);
        props.put("rebalance.backoff.ms", rbBackOff);
        props.put("zookeeper.sync.time.ms", syncTime);
        props.put("auto.commit.interval.ms", autoCommit);
        return new ConsumerConfig(props);
    }

    public void stop() {
        executors.shutdown();
        if (connector != null) {
            connector.shutdown();
        }
    }
}

