package devicegate.launch;

import devicegate.actor.MasterActor;
import devicegate.conf.Configure;
import devicegate.conf.V;
import devicegate.manager.MachineManager;
import org.apache.log4j.Logger;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    //private volatile boolean needPing;

    //private final String addrSnapshotPath;

    public MasterLaunch(Configure conf) {
        this.conf = conf;
        this.masterActor = new MasterActor(conf);
        this.mm = MachineManager.getInstance();
        this.state = new AtomicInteger();
        this.es = new ScheduledThreadPoolExecutor(1);
        //this.needPing = false;
        //this.addrSnapshotPath = conf.getStringOrElse(V.MASTER_ADDR_SNAPSHOT, "snapshot/addr.spt");
    }

    public void launch() throws Exception{
        if (state.compareAndSet(0, 1)) {
            masterActor.start();
            //mm.loadFrom(masterActor, addrSnapshotPath);
            startShedualTask();
        } else {
            throw new RuntimeException("Failed to launch in state: " + state.get());
        }
    }

    public void shutdown() {
        if (state.compareAndSet(1, 2)) {
            es.shutdown();
            masterActor.stop();
            //mm.storeTo(addrSnapshotPath);
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
                mm.showAllAddress();
            }
        }, delay, period, TimeUnit.MILLISECONDS);
    }
}
