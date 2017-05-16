package devicegate.slave;

import devicegate.actor.SlaveActor;
import devicegate.actor.message.AddIdMessage;
import devicegate.actor.message.RmIdMessage;
import devicegate.conf.Configure;
import devicegate.conf.V;
import devicegate.manager.ChannelManager;
import devicegate.netty.NettyServer;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import net.sf.json.JSONObject;
import sun.plugin2.message.Message;

/**
 * Created by xiaoke on 17-5-16.
 */
public class SlaveLaunch {

    private final Configure conf;

    private final NettyServer nettyServer;

    private final ChannelManager cm;

    private final SlaveActor slaveActor;

    public SlaveLaunch(Configure conf) {
        this.conf = conf;
        this.nettyServer = new NettyServer(this, conf);
        this.cm = ChannelManager.getInstance();
        this.slaveActor = new SlaveActor(conf);
    }

    public boolean addChannel(String id, Channel channel) {
        if (channel == null) {
            return false;
        }
        // added to local manager
        if (cm.putIfAbsent(id, channel) == null) {
            // added to remote manager
            AddIdMessage message = new AddIdMessage();
            message.setId(id);
            message.setAddress(nettyServer.getBindAddress());
            slaveActor.sendToMaster(message);
            // bind id to channel
            Attribute<String> attr = channel.attr(AttributeKey.<String>valueOf(V.CHANNEL_ATTR_KEY));
            if (attr != null) {
                attr.setIfAbsent(id);
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    public boolean removeChannel(Channel channel) {
        if (channel == null) {
            return false;
        }
        //cancel id to channel
        Attribute<String> attr = channel.attr(AttributeKey.<String>valueOf(V.CHANNEL_ATTR_KEY));
        if (attr != null) {
            String id = attr.get();
            if (id != null) {
                // remove from local
                if (cm.remove(id) != null) {
                    // remove from remote
                    RmIdMessage message = new RmIdMessage();
                    message.setId(id);
                    slaveActor.sendToMaster(message);
                }
            }
        }
        return true;
    }

    public void pushToKafka(JSONObject jo) {

    }

}
