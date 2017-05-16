package devicegate.manager;

import org.apache.log4j.Logger;

import java.net.InetSocketAddress;

/**
 * Created by xiaoke on 17-5-16.
 */
class Address {

    private final InetSocketAddress socketAddress;

    private final long timeVersion;

    public Address(InetSocketAddress isa, long tv) {
        this.socketAddress = isa;
        this.timeVersion = tv;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public long getTimeVersion() {
        return timeVersion;
    }

}

public class MachineManager extends AbstactManager<Address>{

    private static final Logger log = Logger.getLogger(ChannelManager.class);

    private static MachineManager mm = null;

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

    public void update(String id, Address address) {
        Address old = get(id);
        if (old == null) {
            put(id, address);
        } else {
            if (old.getTimeVersion() < address.getTimeVersion()) {
                put(id, address);
            }
        }
    }
    @Override
    void afterRemoved(Address oldValue) {
    }
}
