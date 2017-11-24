package devicegate.actor;

import akka.actor.ActorSelection;
import devicegate.conf.Configure;
import devicegate.conf.V;

import java.net.InetSocketAddress;

/**
 * Created by xiaoke on 17-5-18.
 */
abstract public class AbstractAkkaActor {

    protected final Configure conf;

    public AbstractAkkaActor(Configure conf) {
        this.conf = conf;
    }

    protected String getRemoteActorPath(InetSocketAddress remoteSystemAddr) {
        int masterPort = conf.getIntOrElse(V.ACTOR_MASTER_PORT);
        String remoteHost;
        int remotePort;
        if (remoteSystemAddr == null) {
            remoteHost = conf.getStringOrElse(V.MASTER_HOST);
            remotePort = masterPort;
        } else {
            remoteHost = remoteSystemAddr.getAddress().getHostAddress();
            remotePort = remoteSystemAddr.getPort();
        }
        String remoteName;
        if (remotePort == masterPort) {
            remoteName = conf.getStringOrElse(V.ACTOR_MASTER_SYSTEM_NAME);
        } else {
            remoteName = conf.getStringOrElse(V.ACTOR_SLAVE_SYSTEM_NAME);
        }
        String remotePath = conf.getStringOrElse(V.ACTOR_INSTANCE_PATH);
        String remoteActorPath = "akka.tcp://" + remoteName + "@" + remoteHost + ":" + remotePort + "/user/" + remotePath;
        return remoteActorPath;
    }

    abstract void start() throws Exception;

    abstract void stop();

    abstract void sendToRemote(Object msg, InetSocketAddress remoteSystemAddr);

    abstract InetSocketAddress systemAddress();

}
