package devicegate.conf;

/**
 * Created by xiaoke on 17-5-16.
 */
public class V {
    // used for para info
    public static final String LOG_PATH = "src/file/log4j.properties" ;
    public static final String VAR_FILE_PATH = "src/file/var.xml";
    public static final String NETTY_CHANNEL_ATTR_KEY = "CHANNEL_ID_ATTACHMENT";

    // used for base
    public static final String MASTER_HOST = "sjtumaster.master.host";
    public static final String SLAVE_HOST= "sjtumaster.slave.host";
    public static final String MASTER_ADDR_SNAPSHOT = "sjtumaster.master.addr.snapshot";
    public static final String MASTER_SCHELDULE_DELAY = "sjtumaster.master.schedule.delay";
    public static final String MASTER_SCHELDULE_PERIOD = "sjtumaster.master.schedule.period";

    // used for netty
    public static final String NETTY_SERVER_SO_BACKLOG = "sjtumaster.netty.server.sobacklog";
    public static final String NETTY_SLAVE_SERVER_PORT= "sjtumaster.netty.slave.port";
    //public static final String NETTY_CHANNEL_ATTR_KEY = "sjtumaster.netty.channel.attr.key";

    // used for actor
    public static final String ACTOR_MASTER_SYSTEM_NAME = "sjtumaster.master.system.name";
    public static final String ACTOR_SLAVE_SYSTEM_NAME = "sjtumaster.slave.system.name";
    public static final String ACTOR_INSTANCE_PATH = "sjtumaster.actor.instance.path";

    public static final String ACTOR_MASTER_PORT = "sjtumaster.actor.master.port";
    public static final String ACTOR_SLAVE_PORT = "sjtumaster.actor.master.port";
    public static final String ACTOR_TELLME_MAX_IDS = "sjtumaster.actor.tellme.max.ids";
    public static final String ACTOR_REPLY_TIMEOUT = "sjtumaster.actor.reply.timeout";

    public static final String ACTOR_CONF_FILE_PATH = "sjtumaster.actor.conf.file";

    // used for kafka
    public static final String KAFKA_QUEUE_COMPACITY = "sjtumaster.kafka.queue.compacity";
    public static final String KAFKA_FULL_DROP = "sjtumaster.kafka.full.drop";
    public static final String KAFKA_PUSH_TOPIC = "sjtumaster.kafka.push.topic";
    public static final String KAFKA_BROKER_LIST = "sjtumater.kafka.broker.list";
    public static final String KAFAK_CLOSING_WAITTIME = "sjtumaster.kafka.closing.waittime";

    // used for mqtt
    public static final String MQTT_CONNECT_TIMEOUT = "sjtumaster.mqtt.connect.timeout";
    public static final String MQTT_KEEPALIVE_INTERVAL = "sjtumaster.mqtt.keepalive.interval";
    public static final String MQTT_RECONNECT_PERIOD = "sjtumaster.mqtt.reconnect.period";
    public static final String MQTT_CLIENT_ID = "sjtumaster.mqtt.client.id";
    public static final String MQTT_SUB_TOPIC = "sjtumaster.mqtt.sub.topic";
}
