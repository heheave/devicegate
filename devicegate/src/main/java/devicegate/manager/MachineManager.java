package devicegate.manager;

import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * Created by xiaoke on 17-5-16.
 */
public class MachineManager extends AbstactManager<String, MachineCacheInfo>{

    private static class TimeVersion {

        long timeVersion;

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

//    public void loadFrom(MasterActor ma, String path) {
//        File file = new File(path);
//        if (!file.exists()) {
//            return;
//        }
//        BufferedReader br = null;
//        try {
//            br = new BufferedReader(new FileReader(file));
//            String line = null;
//            while ((line = br.readLine()) != null) {
//                try {
//                    String[] infos = line.split(":", 2);
//                    InetSocketAddress tmpAddr = new InetSocketAddress(infos[0], Integer.parseInt(infos[1]));
//                    ma.sendToRemote(MessageFactory.getMessage(Msg.TYPE.HB), tmpAddr);
//                }catch (Exception e) {
//                    e.fillInStackTrace();
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    public void storeTo(String path) {
//        File file = new File(path);
//        PrintWriter pw = null;
//        try {
//            if (!file.exists()) {
//                file.getParentFile().mkdirs();
//                file.createNewFile();
//            }
//            if (file.isFile()) {
//                pw = new PrintWriter(file);
//                synchronized (addrs) {
//                    for (Map.Entry<InetSocketAddress, TimeVersion> entry: addrs.entrySet()) {
//                        InetSocketAddress key = entry.getKey();
//                        String storeStr = key.getAddress().getHostAddress()
//                                + ":" +key.getPort();
//                        pw.println(storeStr);
//                    }
//                    pw.flush();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (pw != null) {
//                pw.close();
//            }
//        }
//    }

    public boolean updateAddress(InetSocketAddress addr) {
        synchronized (addrs) {
            TimeVersion tv = addrs.get(addr);
            if (tv != null) {
                tv.update();
                return true;
            } else {
                addrs.put(addr, new TimeVersion());
                return false;
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

    public void update(String id, InetSocketAddress inetAddr, String ptc) {
        updateAddress(inetAddr);
        put(id, new MachineCacheInfo(inetAddr, ptc));
    }

    public void remove(String id, InetSocketAddress inetAddr) {
        MachineCacheInfo di = idToCacheObj.get(id);
        if (di != null && di.getIsa().equals(inetAddr)) {
            MachineCacheInfo newDi = new MachineCacheInfo(inetAddr, di.getPtc());
            if (idToCacheObj.remove(id, newDi)) {
                afterRemoved(di);
            }
        }
    }

    public void cleanOldAddress(long timeVersion) {
        // HashMap need synchronized
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

    public Map<String, List<String>> showAllAddress() {
        // HashMap need synchronized
        Map<String, List<String>>  result = new HashMap<String, List<String>>();
        synchronized (addrs) {
            for (InetSocketAddress isa : addrs.keySet()) {
                String resKey = isa.getAddress().getHostAddress() +":" + isa.getPort();
                List<String> tmp = new LinkedList<String>();
                tmp.add(String.valueOf(addrs.get(isa).timeVersion));
                result.put(resKey, tmp);
                //log.info("\t\tMachine (" + isa.getAddress().getHostAddress() +":" + isa.getPort()+ "): " + addrs.get(isa).timeVersion);
            }
        }

        for (Map.Entry<String, MachineCacheInfo> entry : idToCacheObj.entrySet()) {
            if (entry.getValue() != null) {
                InetSocketAddress isa = entry.getValue().getIsa();
                if (isa != null) {
                    String resKey = isa.getAddress().getHostAddress() +":" + isa.getPort();
                    List<String> tmp = result.get(resKey);
                    if (tmp != null) {
                        tmp.add(entry.getKey() + "," + entry.getValue().getPtc());
                    }
//                            log.info("\t\tDevice (" + entry.getKey() + ") is on machine: "
//                            + isa.getAddress().getHostAddress()
//                            + ":" + isa.getPort() + " using " + entry.getValue().getPtc() + " protocol");
                }
            }
        }

        return result;
    }

    private void removeAll(InetSocketAddress inetAddr) {
        // ConcurrentHashMap doesn't neet synchronized
        // however entry.value may be null under concurrency condition
        List<String> toRemoveKeys = new LinkedList<String>();
        for (Map.Entry<String, MachineCacheInfo> entry : idToCacheObj.entrySet()) {
            if (entry.getValue() != null && inetAddr.equals(entry.getValue().getIsa())) {
                toRemoveKeys.add(entry.getKey());
            }
        }
        for (String id: toRemoveKeys) {
            remove(id);
        }
        toRemoveKeys.clear();
    }

    @Override
    void afterRemoved(MachineCacheInfo oldValue) {
        InetSocketAddress addr = oldValue.getIsa();
        if (addr != null) {
            log.info("Channel to " + addr.getAddress().getHostAddress() + ":" + addr.getPort() + " is removed");
        }
    }
}
