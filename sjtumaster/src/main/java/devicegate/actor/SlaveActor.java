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
public class SlaveActor {

    private final Configure conf;

    private final ActorSystem system;

    private final ActorRef actorRef;

    public SlaveActor(Configure conf) {
        this.conf = conf;
        String slaveName = conf.getStringOrElse(V.SLAVE_SYSTEM_NAME, "SLAVESYSTEM");
        Config config = ConfigFactory.load().getConfig("localConf");
        system = ActorSystem.apply(slaveName, config);
        String slavePath = conf.getStringOrElse(V.ACTOR_INSTANCE_PATH, "SLAVEPATH");
        actorRef = system.actorOf(Props.create(SlaveHandler.class), slavePath);
    }

    public void sendToMaster(Object msg) {
        String remoteActorPath = getRemoteActorPath(conf);
        ActorSelection remoteActor = system.actorSelection(remoteActorPath);
        System.out.println(remoteActorPath);
        remoteActor.tell(msg, actorRef);
    }

    public static void main(String[] args) {
        SlaveActor sa = new SlaveActor(new Configure());
        sa.sendToMaster("hello master");
    }

    public static String getRemoteActorPath(Configure conf) {
        String remoteHost = conf.getStringOrElse(V.MASTER_SERVER_HOST, "127.0.0.1");
        int remotePort = conf.getIntOrElse(V.ACTOR_INSTANCE_PORT, 10010);
        String remoteName = conf.getStringOrElse(V.MASTER_SYSTEM_NAME, "MASTERSYSTEM");
        String remotePath = conf.getStringOrElse(V.ACTOR_INSTANCE_PATH, "MASTERPATH");
        String remoteActorPath = "akka.tcp://" +remoteName+ "@"+remoteHost+":"+remotePort+"/user/" + remotePath;
        return remoteActorPath;
    }
}
