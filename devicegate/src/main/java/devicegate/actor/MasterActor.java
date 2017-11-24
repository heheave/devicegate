package devicegate.actor;

import akka.actor.*;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import devicegate.actor.message.Msg;
import devicegate.conf.Configure;
import devicegate.conf.V;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.io.File;
import java.net.InetSocketAddress;

/**
 * Created by xiaoke on 17-5-16.
 */
public class MasterActor extends AbstractAkkaActor {

    private ActorSystem system;

    private ActorRef actorRef;

    private final InetSocketAddress systemAddress;

    public MasterActor(Configure conf) {
        super(conf);
        String systemHost = conf.getStringOrElse(V.MASTER_HOST);
        int port = conf.getIntOrElse(V.ACTOR_MASTER_PORT);
        this.systemAddress = new InetSocketAddress(systemHost, port);
    }

    public void sendToRemote(Object msg, InetSocketAddress hostAddr) {
        String slaveActorPath = getRemoteActorPath(hostAddr);
        ActorSelection remoteActor = system.actorSelection(slaveActorPath);
        remoteActor.tell(msg, actorRef);
    }

    public Object sendToSlaveWithReply(Msg msg, InetSocketAddress hostAddr)  throws Exception {
        long contTimeOut = conf.getLongOrElse(V.ACTOR_REPLY_TIMEOUT);
        return sendToSlaveWithReply(msg, hostAddr, contTimeOut);
    }

    public Object sendToSlaveWithReply(Msg msg, InetSocketAddress hostAddr, long timeOut) throws Exception {
        msg.setRet(true);
        String slaveActorPath = getRemoteActorPath(hostAddr);
        ActorSelection remoteActor = system.actorSelection(slaveActorPath);
        Timeout timeout = Timeout.longToTimeout(timeOut);
        Future<Object> future = Patterns.ask(remoteActor, msg, timeout);
        return Await.result(future, timeout.duration());
    }

    public void start() {
        String masterName = conf.getStringOrElse(V.ACTOR_MASTER_SYSTEM_NAME);
        Config config = ConfigFactory.parseFile(new File(V.ACTOR_CONF_PATH)).getConfig("masterConf");
        system = ActorSystem.apply(masterName, config);
        String masterPath = conf.getStringOrElse(V.ACTOR_INSTANCE_PATH);
        actorRef = system.actorOf(Props.create(MasterHandler.class, this), masterPath);
    }

    public void stop() {
        if (system != null) {
            if (actorRef != null) {
                system.stop(actorRef);
            }
            if (!system.isTerminated()) {
                system.shutdown();
            }
        }
    }

    public InetSocketAddress systemAddress() {
        return systemAddress;
    }

//    public static void main(String[] args) {
//        MasterActor ma = new MasterActor(new Configure());
//        ma.start();
//        ma.sendToRemote("dafasf", new InetSocketAddress("127.0.0.1", 10020));
//    }
}
