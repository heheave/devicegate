package devicegate.actor;

import akka.actor.*;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import devicegate.actor.message.AckMessage;
import devicegate.actor.message.Msg;
import devicegate.conf.Configure;
import devicegate.conf.V;
import devicegate.launch.SlaveLaunch;
import org.apache.log4j.Logger;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.net.InetSocketAddress;

/**
 * Created by xiaoke on 17-5-16.
 */
public class SlaveActor extends AbstractAkkaActor{

    private static final Logger log = Logger.getLogger(SlaveActor.class);

    private ActorSystem system;

    private ActorRef actorRef;

    private final SlaveLaunch slaveLaunch;

    private final InetSocketAddress systemAddress;

    public SlaveActor(Configure conf, SlaveLaunch slaveLaunch) {
        super(conf);
        this.slaveLaunch = slaveLaunch;
        String systemHost = conf.getStringOrElse(V.SLAVE_HOST);
        int port = conf.getIntOrElse(V.ACTOR_SLAVE_PORT);
        this.systemAddress = new InetSocketAddress(systemHost, port);
    }

    public void sendToRemote(Object msg, InetSocketAddress remoteSystemAddr) {
        String remoteActorPath = getRemoteActorPath(null);
        ActorSelection remoteActor = system.actorSelection(remoteActorPath);
        remoteActor.tell(msg, actorRef);
    }

    public Object sendToMasterWithReply(Msg msg)  throws Exception {
        long contTimeOut = conf.getLongOrElse(V.ACTOR_REPLY_TIMEOUT);
        return sendToMasterWithReply(msg, contTimeOut);
    }

    public Object sendToMasterWithReply(Msg msg, long timeOut) throws Exception {
        msg.setRet(true);
        String slaveActorPath = getRemoteActorPath(null);
        ActorSelection remoteActor = system.actorSelection(slaveActorPath);
        Timeout timeout = Timeout.longToTimeout(timeOut);
        Future<Object> future = Patterns.ask(remoteActor, msg, timeout);
        return Await.result(future, timeout.duration());
    }



    public void start() {
        String slaveName = conf.getStringOrElse(V.ACTOR_SLAVE_SYSTEM_NAME);
        //Config config = ConfigFactory.load().getConfig("localConf");
        Config config = ConfigFactory.parseFile(new File(V.ACTOR_CONF_PATH)).getConfig("slaveConf");
        system = ActorSystem.apply(slaveName, config);
        String slavePath = conf.getStringOrElse(V.ACTOR_INSTANCE_PATH);
        actorRef = system.actorOf(Props.create(SlaveHandler.class, slaveLaunch, systemAddress), slavePath);
    }

    public void stop() {
        if (system != null) {
            if (actorRef != null) {
                system.stop(actorRef);
                actorRef = null;
            }
            if (!system.isTerminated()) {
                system.shutdown();
            }
            system = null;
        }
        log.info("Actor has been shutdown");
    }

    public InetSocketAddress systemAddress() {
        return systemAddress;
    }

    public static void main(String[] args) {
        SlaveActor sa = new SlaveActor(new Configure(), null);
        sa.start();
    }
}
