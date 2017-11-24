package devicegate.conf;

import java.util.UUID;

/**
 * Created by xiaoke on 17-5-16.
 */
public class V {
    // used for para info
    public static final String LOG_PATH = "file/log4j.properties";
    public static final String VAR_FILE_PATH = "file/var.xml";
    public static final String ACTOR_CONF_PATH = "file/application.conf";
    public static final String NETTY_CHANNEL_ATTR_KEY = "CHANNEL_ID_ATTACHMENT";

    public static final Mark<String> DEVICE_NOT_AUTH = Mark.makeMark("sjtumaster.device.not.auth", "DEVICE NOT AUTH");
    public static final Mark<String> DEVICE_MSG_ACK = Mark.makeMark("sjtumaster.device.msg.ack", "CNT SUCCESS");

    // used for base
    public static final Mark<String> MASTER_HOST = Mark.makeMark("sjtumaster.master.host", "192.168.31.110");
    public static final Mark<String> SLAVE_HOST= Mark.makeMark("sjtumaster.slave.host", "192.168.31.110");
    public static final Mark<Long> SLAVE_SESSION_TIMEOUT = Mark.makeMark("sjtumaster.salve.session.timeout", 30000L);
    public static final Mark<Long> MASTER_SCHELDULE_DELAY = Mark.makeMark("sjtumaster.master.schedule.delay", 1000L);
    public static final Mark<Long> MASTER_SCHELDULE_PERIOD = Mark.makeMark("sjtumaster.master.schedule.period", 30000L);

    // used for netty
    public static final Mark<Integer> NETTY_SERVER_SO_BACKLOG = Mark.makeMark("sjtumaster.netty.server.sobacklog", 100);
    public static final Mark<Integer> NETTY_MASTER_SERVER_PORT = Mark.makeMark("sjtumaster.netty.master.port", 9090);
    public static final Mark<Integer> NETTY_SLAVE_SERVER_PORT= Mark.makeMark("sjtumaster.netty.slave.port", 10000);
    public static final Mark<Integer> MASTER_HOA_MAX_CONTENT_LENGTH = Mark.makeMark("sjtumaster.hop.max.content.length", 65536);

    // used for actor
    public static final Mark<String> ACTOR_MASTER_SYSTEM_NAME = Mark.makeMark("sjtumaster.master.system.name", "MASTERSYSTEM");
    public static final Mark<String> ACTOR_SLAVE_SYSTEM_NAME = Mark.makeMark("sjtumaster.slave.system.name", "SLAVESYSTEM");
    public static final Mark<String> ACTOR_INSTANCE_PATH = Mark.makeMark("sjtumaster.actor.instance.path", "ACTORPATH");

    public static final Mark<Integer> ACTOR_MASTER_PORT = Mark.makeMark("sjtumaster.actor.master.port", 10010);
    public static final Mark<Integer> ACTOR_SLAVE_PORT = Mark.makeMark("sjtumaster.actor.slave.port", 10020);
    public static final Mark<Integer> ACTOR_TELLME_MAX_IDS = Mark.makeMark("sjtumaster.actor.tellme.max.ids", 50);
    public static final Mark<Long> ACTOR_REPLY_TIMEOUT = Mark.makeMark("sjtumaster.actor.reply.timeout", 2000L);

    //public static final Mark<String> ACTOR_CONF_FILE_PATH = Mark.makeMark("sjtumaster.actor.conf.file");

    // used for kafka
    public static final Mark<Integer> KAFKA_QUEUE_COMPACITY = Mark.makeMark("sjtumaster.kafka.queue.compacity", 10000);
    public static final Mark<Boolean> KAFKA_FULL_DROP = Mark.makeMark("sjtumaster.kafka.full.drop", true);
    public static final Mark<String> KAFKA_PUSH_TOPIC = Mark.makeMark("sjtumaster.kafka.push.topic", "devicegate-topic");
    public static final Mark<String> KAFKA_CTRL_TOPIC = Mark.makeMark("sjtumaster.kafka.ctrl.topic");
    public static final Mark<String> KAFKA_ZK_LIST = Mark.makeMark("sjtumaster.kafka.zk.list", "192.168.1.110:2181");
    public static final Mark<String> KAFKA_BROKER_LIST = Mark.makeMark("sjtumater.kafka.broker.list", "192.168.1.110:9092");
    public static final Mark<Long> KAFKA_CLOSING_WAITTIME = Mark.makeMark("sjtumaster.kafka.closing.waittime", 1000L);
    public static final Mark<String> KAFKA_GROUP_ID = Mark.makeMark("sjtumaster.kafka.group.id", "CTRL_CONSUMER_GROUP");
    public static final Mark<String> KAFKA_ZK_SESSION_TIMEOUT = Mark.makeMark("sjtumaster.kafka.dislock.session.timeout", "7000");
    public static final Mark<String> KAFKA_AUTO_OFFSET_RESET = Mark.makeMark("sjtumaster.auto.offset.reset", "largest");
    public static final Mark<String> KAFKA_ZK_SYNC_TIME = Mark.makeMark("sjtumaster.kafka.dislock.sync.time", "2000");
    public static final Mark<String> KAFKA_REBALANCE_MAX_RETRIES = Mark.makeMark("sjtumaster.kafka.rebalance.max.retries", "4");
    public static final Mark<String> KAFKA_REBALANCE_BACKOFF = Mark.makeMark("sjtumaster.kafka.rebalance.backoff", "2000");
    public static final Mark<String> KAFKA_AUTO_COMMIT_INTERVAL = Mark.makeMark("sjtumaster.auto.commit.interval", "1000");

    // used for mqtt
    public static final Mark<Integer> MQTT_CONNECT_TIMEOUT = Mark.makeMark("sjtumaster.mqtt.connect.timeout", 1000);
    public static final Mark<Integer> MQTT_KEEPALIVE_INTERVAL = Mark.makeMark("sjtumaster.mqtt.keepalive.interval", 2000);
    public static final Mark<Long> MQTT_SLEEP_IF_FAILED = Mark.makeMark("sjtumaster.mqtt.sleep.if.failed", 5000L);
    public static final Mark<Long> MQTT_TIME_TO_WAIT = Mark.makeMark("sjtumaster.mqtt.time.to.wait", 4000L);
    public static final Mark<Integer> MQTT_PUB_QUEUE_SIZE = Mark.makeMark("sjtumaster.mqtt.pub.queue.size", 1000);
    public static final Mark<String> MQTT_CLIENT_ID = Mark.makeMark("sjtumaster.mqtt.client.id", UUID.randomUUID().toString());
    public static final Mark<String> MQTT_SUB_TOPIC = Mark.makeMark("sjtumaster.mqtt.sub.topic", "PLATFORM_MQTT_ACCESS_TOPIC");

    // used for controller
    public static final Mark<Integer> CTRL_QUEUE_COMPACITY = Mark.makeMark("sjtumaster.ctrl.queue.compacity", 1000);


    // used for mongodb
    public static final Mark<String> MONGO_DB_HOST = Mark.makeMark("sjtumaster.mongo.db.host", "localhost");
    public static final Mark<Integer> MONGO_DB_PORT = Mark.makeMark("sjtumaster.mongo.db.port", 27017);
    public static final Mark<Integer> MONGO_DB_TIMEOUT = Mark.makeMark("sjtumaster.mongo.db.timeout", 5000);
    public static final Mark<String> MONGO_DBNAME_DEVICECONF = Mark.makeMark("sjtumaster.mongo.dbname.deviceconf", "dcDb");
    public static final Mark<String> MONGO_COLNAME_DEVICECONF = Mark.makeMark("sjtumaster.mongo.colname.deviceconf", "dcCol");
}
