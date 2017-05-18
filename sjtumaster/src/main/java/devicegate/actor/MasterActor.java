package devicegate.actor;

import akka.actor.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import devicegate.conf.Configure;
import devicegate.conf.V;

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
        this.systemAddress = new InetSocketAddress("127.0.0.1", 10010);
    }

    public void sendToRemote(Object msg, InetSocketAddress hostAddr) {
        String slaveActorPath = getRemoteActorPath(hostAddr);
        ActorSelection remoteActor = system.actorSelection(slaveActorPath);
        remoteActor.tell(msg, actorRef);
    }

    public void start() {
        String masterName = conf.getStringOrElse(V.ACTOR_MASTER_SYSTEM_NAME, "MASTERSYSTEM");
        Config config = ConfigFactory.load().getConfig("remoteConf");
        system = ActorSystem.apply(masterName, config);
        String masterPath = conf.getStringOrElse(V.ACTOR_INSTANCE_PATH, "ACTORPATH");
        actorRef = system.actorOf(Props.create(MasterHandler.class), masterPath);
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

    public static void main(String[] args) {
        MasterActor ma = new MasterActor(new Configure());
        ma.start();
        ma.sendToRemote("dafasf", new InetSocketAddress("127.0.0.1", 10020));
    }
}
