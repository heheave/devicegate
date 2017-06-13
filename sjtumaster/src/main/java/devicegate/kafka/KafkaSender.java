package devicegate.kafka;

import devicegate.conf.Configure;
import devicegate.conf.V;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * Created by xiaoke on 17-5-17.
 */
public class KafkaSender {

    private static final Logger log = Logger.getLogger(KafkaSender.class);

    private static final String BROKER_LIST = "192.168.1.111:9092,192.168.1.112:9092,192.168.1.113:9092";

    private final Configure conf;

    private final ExecutorService runner;

    private final BlockingQueue<Serializable> msgQueue;

    private final boolean fullDrop;

    private volatile boolean isRunning;

    private volatile Properties producerPropertis;

    private Producer<String, String> producer;

    private final Runnable run = new Runnable() {
        public void run() {
            final String topic = conf.getStringOrElse(V.KAFKA_PUSH_TOPIC, "devicegate-topic");
            while(isRunning) {
                try {
                    final Serializable msg = msgQueue.take();
                    if (msg != null) {
                        log.info("Sending info is: " + msg.toString());
                        final ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, msg.toString());
                        producer.send(record, new Callback() {
                            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                                if (recordMetadata != null) {
                                    log.info("Msg send: offset=" + recordMetadata.offset() + ", partition=" + recordMetadata.partition());
                                } else {
                                    log.info("Kafka send error", e);
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    log.warn("Take msg from msgQueue error: " + e);
                }
            }
        }
    };

    public KafkaSender(Configure conf) {
        this.conf = conf;
        this.runner = Executors.newSingleThreadExecutor();
        this.fullDrop = conf.getBooleanOrElse(V.KAFKA_FULL_DROP, true);
        int queueCompacity = conf.getIntOrElse(V.KAFKA_QUEUE_COMPACITY, 10000);
        this.msgQueue = new ArrayBlockingQueue<Serializable>(queueCompacity);
        String brokerList = conf.getStringOrElse(V.KAFKA_BROKER_LIST, BROKER_LIST);
        this.producerPropertis = new Properties();
        this.producerPropertis.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        this.producerPropertis.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producerPropertis.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    }

    public boolean msgIn(Serializable msg) {
        if (!isRunning) {
            return false;
        }
        if (fullDrop) {
            return msgQueue.offer(msg);
        } else {
            try {
                msgQueue.put(msg);
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public void start() {
        isRunning = true;
        runner.submit(run);
        producer = new KafkaProducer<String, String>(producerPropertis);
    }

    public void stop() {
        if (producer != null) {
            long sleepTryTime = 0;
            final long sleepTime = conf.getLongOrElse(V.KAFKA_CLOSING_WAITTIME, 1000);
            if (sleepTime > 0) {
                synchronized (this) {
                    while (!msgQueue.isEmpty() && sleepTryTime < 3) {
                        try {
                            wait(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            sleepTryTime++;
                        }
                    }
                }
            }
            isRunning = false;
            runner.shutdown();
            producer.flush();
            producer.close();
            producerPropertis.clear();
        } else {
            isRunning = false;
        }
        if (!msgQueue.isEmpty()) {
            log.info(msgQueue.size() + " messages has been dropped because of shutdown");
            msgQueue.clear();
        }
        log.info("Kafka sender has been stopped");
    }
}
