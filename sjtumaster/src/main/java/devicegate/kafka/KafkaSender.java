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

    private volatile boolean  isRunning;

    private final Properties producerPropertis;

    private final Producer<String, String> producer;

    private final Runnable run = new Runnable() {
        public void run() {
            final String topic = conf.getStringOrElse(V.KAFKA_PUSH_TOPIC, "devicegate-topic");
            while(isRunning) {
                try {
                    Serializable msg = msgQueue.take();
                    if (msg != null) {
                        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, msg.toString());
                        producer.send(record, new Callback() {
                            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                                log.info("Msg send: offset=" + recordMetadata.offset() + ", partition=" + recordMetadata.partition());
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    log.info("Take msg from msgQueue error: " + e);
                }
            }
        }
    };

    public KafkaSender(Configure conf) {
        this.conf = conf;
        this.runner = Executors.newSingleThreadExecutor();
        fullDrop = conf.getBooleanOrElse(V.KAFKA_FULL_DROP, true);
        int queueCompacity = conf.getIntOrElse(V.KAFKA_QUEUE_COMPACITY, 10000);
        this.msgQueue = new ArrayBlockingQueue<Serializable>(queueCompacity);
        String brokerList = conf.getStringOrElse(V.KAFKA_BROKER_LIST, BROKER_LIST);
        producerPropertis = new Properties();
        producerPropertis.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        producerPropertis.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerPropertis.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<String, String>(producerPropertis);
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
    }

    public void stop() {
        long sleepTryTime = 0;
        final long sleepTime = conf.getLongOrElse(V.KAFAK_CLOSING_WAITTIME, 1000);
        if (sleepTime > 0) {
            synchronized (this) {
                while(!msgQueue.isEmpty() && sleepTryTime < 3) {
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
        if (!msgQueue.isEmpty()) {
            log.info(msgQueue.size() + " messages has been dropped because of shutdown");
        }
        isRunning = false;
        runner.shutdown();
        producer.flush();
        producer.close();
        log.info("Kafka sender has been stopped");
    }
}
