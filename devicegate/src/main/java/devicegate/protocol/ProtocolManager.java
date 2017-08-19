package devicegate.protocol;

import devicegate.conf.Configure;
import devicegate.launch.SlaveLaunch;
import net.sf.json.JSONObject;
import org.apache.commons.collections.functors.ExceptionClosure;
import org.apache.log4j.Logger;

/**
 * Created by xiaoke on 17-8-19.
 */
abstract public class ProtocolManager {

    public enum ProtocolType {
        TCP, MQTT
    }

    public enum AuthType {
        CNT, IN, OUT
    }

    private static final Logger log = Logger.getLogger(ProtocolManager.class);

    protected final SlaveLaunch slaveLaunch;

    protected final Configure conf;

    protected final MessageServer messageServer;

    public SlaveLaunch slaveLaunch() {
        return slaveLaunch;
    }

    protected ProtocolManager(MessageServer messageServer, SlaveLaunch slaveLaunch, Configure conf) {
        this.slaveLaunch = slaveLaunch;
        this.conf = conf;
        this.messageServer = messageServer;
    }

    public abstract AuthRet authorize(JSONObject jo, AuthType type);
    public abstract ProtocolType getProtocolType();
    public abstract void mesErrorTackle(MessageException e);

    public boolean messageOut(JSONObject jo, AttachInfo attachInfo) {
        AuthRet authRet = authorize(jo, AuthType.OUT);
        if (authRet.isAuthorized()) {
            try {
                messageServer.sendMessage(jo, attachInfo);
                return true;
            } catch (MessageException e) {
                log.error("Sending " + getProtocolType().name() + " message error", e);
                if (e.isNeedTackle()) {
                    mesErrorTackle(e);
                }
                return false;
            } catch (Exception e) {
                log.error("Sending " + getProtocolType().name() + " message error", e);
                return false;
            }
        } else {
            log.warn("Sending " + jo + " failed because of authorize failed, because of " + authRet.faildReason());
            return false;
        }
    }


    public void initial() {
        try {
            messageServer.start();
        } catch (Exception e) {
            log.error("Initialing " + getProtocolType().name() + " MessageServer error", e);
        }
    }

    public void shutdown() {
        try {
            messageServer.stop();
        } catch (Exception e) {
            log.error("Shutdown " + getProtocolType().name() + " MessageServer error", e);
        }
    }
}
