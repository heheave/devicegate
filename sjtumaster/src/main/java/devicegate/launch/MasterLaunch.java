package devicegate.launch;

import devicegate.actor.MasterActor;
import devicegate.conf.Configure;
import devicegate.conf.V;
import devicegate.kafka.KafkaReceiver;
import devicegate.manager.MachineCacheInfo;
import devicegate.manager.MachineManager;
import devicegate.netty.MasterNettyServer;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import devicegate.manager.MachineManager.*;
/**
 * Created by xiaoke on 17-5-16.
 */
public class MasterLaunch implements Launch{

    private static final Logger log = Logger.getLogger(MasterLaunch.class);

    private final AtomicInteger state;

    private final Configure conf;

    private final MasterActor masterActor;

    private final MachineManager mm;

    private final ScheduledThreadPoolExecutor es;

    private final MasterNettyServer nettyServer;

    //private final KafkaReceiver ctrlReceiver;
    //private volatile boolean needPing;

    //private final String addrSnapshotPath;

    public MasterLaunch(Configure conf) {
        this.conf = conf;
        this.masterActor = new MasterActor(conf);
        this.mm = MachineManager.getInstance();
        this.nettyServer = new MasterNettyServer(this);
        //this.ctrlReceiver = new KafkaReceiver(this);
        this.es = new ScheduledThreadPoolExecutor(1);
        this.state = new AtomicInteger(0);
    }

    public void launch() throws Exception{
        if (state.compareAndSet(0, 1)) {
            masterActor.start();
            nettyServer.start();
            //ctrlReceiver.start();
            startShedualTask();
        } else {
            throw new RuntimeException("Failed to launch in state: " + state.get());
        }
    }

    public void shutdown() {
        if (state.compareAndSet(1, 2)) {
            mm.clear();
            es.shutdown();
            //ctrlReceiver.stop();
            nettyServer.stop();
            masterActor.stop();
        } else {
            throw new RuntimeException("Failed to shutdown in state: " + state.get());
        }
    }

    public int state() {
        return state.get();
    }

    private void startShedualTask() {
        long delay = conf.getLongOrElse(V.MASTER_SCHELDULE_DELAY, 1000);
        final long period = conf.getLongOrElse(V.MASTER_SCHELDULE_PERIOD, 10000);
        es.scheduleAtFixedRate(new Runnable() {
            public void run() {
                mm.cleanOldAddress(System.currentTimeMillis() - (period << 1));
            }
        }, delay, period, TimeUnit.MILLISECONDS);
    }

    public void tellMachineByDid(String did, Object obj) {
        MachineCacheInfo mci = mm.get(did);
        if (mci != null && mci.getIsa() != null) {
            masterActor.sendToRemote(obj, mci.getIsa());
        }
    }

    public Configure getConf() {
        return conf;
    }

    public MasterActor getMasterActor() {
        return masterActor;
    }

    public MachineManager getMm() {
        return mm;
    }

    public MasterNettyServer getNettyServer() {
        return nettyServer;
    }

//    public KafkaReceiver getCtrlReceiver() {
//        return ctrlReceiver;
//    }
}
