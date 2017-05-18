package devicegate.manager;

import devicegate.actor.MasterActor;
import devicegate.actor.message.MessageFactory;
import devicegate.actor.message.Msg;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * Created by xiaoke on 17-5-16.
 */

public class MachineManager extends AbstactManager<InetSocketAddress>{

    private static class TimeVersion {

        private long timeVersion;

        public TimeVersion() {
            this(System.currentTimeMillis());
        }

        public TimeVersion(long timeVersion) {
            this.timeVersion = timeVersion;
        }

        public synchronized void update() {
            update(System.currentTimeMillis());
        }

        public synchronized void update(long timeVersion) {
             if (this.timeVersion < timeVersion) {
                 this.timeVersion = timeVersion;
             }
        }
    }
    private static final Logger log = Logger.getLogger(DeviceManager.class);

    private static MachineManager mm = null;

    private final Map<InetSocketAddress, TimeVersion> addrs;

    private MachineManager() {
        super();
        this.addrs = new HashMap<InetSocketAddress, TimeVersion>();
    }

    public static MachineManager getInstance() {
        if (mm == null) {
            synchronized (MachineManager.class) {
                if (mm == null) {
                    mm = new MachineManager();
                }
            }
        }
        return mm;
    }

    public void updateAddress(InetSocketAddress addr) {
        synchronized (addrs) {
            TimeVersion tv = addrs.get(addr);
            if (tv != null) {
                tv.update();
            } else {
                addrs.put(addr, new TimeVersion());
            }
        }
    }

    public void removeAddress(InetSocketAddress addr) {
        synchronized (addrs) {
            TimeVersion tv = addrs.remove(addr);
            if (tv != null) {
                removeAll(addr);
            }
        }
    }

    public void update(String id, InetSocketAddress inetAddr) {
        updateAddress(inetAddr);
        put(id, inetAddr);
    }

    public void cleanOldAddress(long timeVersion) {
        synchronized (addrs) {
            List<InetSocketAddress> toRemoveKeys = new LinkedList<InetSocketAddress>();
            for (Map.Entry<InetSocketAddress, TimeVersion> entry: addrs.entrySet()) {
                if (entry.getValue().timeVersion < timeVersion) {
                    toRemoveKeys.add(entry.getKey());
                }
            }

            for(InetSocketAddress isa: toRemoveKeys) {
                removeAddress(isa);
            }
            toRemoveKeys.clear();
        }

    }

    private void removeAll(InetSocketAddress inetAddr) {
        synchronized (idToCacheObj) {
            List<String> toRemoveKeys = new LinkedList<String>();
            for (Map.Entry<String, InetSocketAddress> entry : idToCacheObj.entrySet()) {
                if (inetAddr.equals(entry.getValue())) {
                    toRemoveKeys.add(entry.getKey());
                }
            }
            for (String id: toRemoveKeys) {
                remove(id);
            }
            toRemoveKeys.clear();
        }
    }

    public void pingAllAddress(MasterActor ma) {
        for (InetSocketAddress isa: addrs.keySet()) {
            Msg hbMsg = MessageFactory.getMessage(Msg.TYPE.HB);
            ma.sendToRemote(hbMsg, isa);
        }
    }

    @Override
    void afterRemoved(InetSocketAddress oldValue) {
        log.info("Channel to " + oldValue.getHostName() + ":" + oldValue.getPort() + " is removed");
    }
}
