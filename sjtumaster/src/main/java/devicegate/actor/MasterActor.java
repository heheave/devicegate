package devicegate.actor;

import akka.actor.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import devicegate.conf.Configure;
import devicegate.conf.V;

import java.io.File;

/**
 * Created by xiaoke on 17-5-16.
 */
public class MasterActor {

    private final Configure conf;

    private final ActorSystem system;

    private final ActorRef actorRef;

    public MasterActor(Configure conf) {
        this.conf = conf;
        String masterName = conf.getStringOrElse(V.MASTER_SYSTEM_NAME, "MASTERSYSTEM");
        Config config = ConfigFactory.load().getConfig("remoteConf");
        system = ActorSystem.apply(masterName, config);
        String masterPath = conf.getStringOrElse(V.ACTOR_INSTANCE_PATH, "MASTERPATH");
        actorRef = system.actorOf(Props.create(MasterHandler.class), masterPath);
        System.out.println(actorRef.path().toStringWithAddress(actorRef.path().address()));
    }

    public void sendToSlave(Object msg, String host) {
        String slaveActorPath = getRemoteActorPath(conf, host);
        ActorSelection remoteActor = system.actorSelection(slaveActorPath);
        System.out.println(slaveActorPath);
        remoteActor.tell(msg, actorRef);
    }

    public static void main(String[] args) {
        MasterActor ma = new MasterActor(new Configure());
    }

    public static String getRemoteActorPath(Configure conf, String host) {
        String remoteHost = host;
        int remotePort = conf.getIntOrElse(V.ACTOR_INSTANCE_PORT, 10020);
        String remoteName = conf.getStringOrElse(V.SLAVE_SYSTEM_NAME, "SLAVESYSTEM");
        String remotePath = conf.getStringOrElse(V.ACTOR_INSTANCE_PATH, "SLAVEPATH");
        String remoteActorPath = "akka.tcp://" +remoteName+ "@"+remoteHost+":"+remotePort+"/user/" + remotePath;
        return remoteActorPath;
    }
}
