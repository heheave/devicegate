package simulation;

import org.apache.log4j.Logger;
import simulation.DeviceValue.AbstractDeviceValue;
import simulation.device.Device;
import simulation.device.DeviceFactory;
import simulation.view.DeviceInstance;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaoke on 17-5-15.
 */
public class DeviceMain {

    private static Random ran = new Random();

    private static final Logger log = Logger.getLogger(DeviceMain.class);



    private static class SendRun implements Runnable {

        private final DeviceInstance<AbstractDeviceValue<?>> device;

        public SendRun(String app, String dmark, Device.TYPE dtype, int portNum) {
            device = DeviceFactory.getMonitorDevice(dtype, app, dmark, portNum);
            if (device == null) {
                throw new NullPointerException(String.format("%s:%s already exists", app, dmark));
            }
            device.start();
        }

        public void run() {
            device.simulateOnce();
        }

        public void stop() {
            device.close();
        }

    }

    final static ScheduledExecutorService es = Executors.newScheduledThreadPool(3);
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                es.shutdown();
                log.info("shutdown!!!");
            }
        });
    }

    public static void addNewDevice(String app, String dmark, String type, int portNum) {
        //log.info("starting...");
        //final SendRun digitalDevice = new SendRun(Device.TYPE.DIGITL, 2);
        //final SendRun analogDevice = new SendRun(Device.TYPE.ANALOG, 4);
        if ("SWITCH".equalsIgnoreCase(type)) {
            SendRun sr = new SendRun(app, dmark, Device.TYPE.SWITCH, portNum);
            es.scheduleAtFixedRate(sr, 1000, 15000, TimeUnit.MILLISECONDS);
        } else if ("DIGITL".equalsIgnoreCase(type)) {
            SendRun sr = new SendRun(app, dmark, Device.TYPE.DIGITL, portNum);
            es.scheduleAtFixedRate(sr, 1000, 15000, TimeUnit.MILLISECONDS);
        } else if ("ANALOG".equalsIgnoreCase(type)) {
            SendRun sr = new SendRun(app, dmark, Device.TYPE.ANALOG, portNum);
            es.scheduleAtFixedRate(sr, 1000, 15000, TimeUnit.MILLISECONDS);
        }
        //return true;
        //JFrame jFrame = new DeviceFrame("ABC001");

    }
}
