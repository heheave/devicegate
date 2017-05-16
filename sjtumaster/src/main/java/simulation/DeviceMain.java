package simulation;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import simulation.DeviceValue.BooleanDeviceValue;
import simulation.DeviceValue.DoubleDeviceValue;
import simulation.DeviceValue.IntegerDeviceValue;
import simulation.device.AbstracMonitorDevice;
import simulation.device.Device;
import simulation.device.DeviceFactory;

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

        private final AbstracMonitorDevice device;

        public SendRun(AbstracMonitorDevice device) {
            this.device = device;
        }

        public void run() {
            try {
                randomSetValues(device);
                log.info(device.toJson().toString());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        PropertyConfigurator.configure("src/file/log4j.properties");
        log.info("starting...");
        AbstracMonitorDevice switchDevice = DeviceFactory.getMonitorDevice(Device.TYPE.SWITCH, 1);
        AbstracMonitorDevice digitalDevice = DeviceFactory.getMonitorDevice(Device.TYPE.DIGITAL, 2);
        AbstracMonitorDevice analogDevice = DeviceFactory.getMonitorDevice(Device.TYPE.ANALOG, 4);
        final ScheduledExecutorService es = Executors.newScheduledThreadPool(3);
        es.scheduleAtFixedRate(new SendRun(switchDevice), 1000, 100, TimeUnit.MILLISECONDS);
        es.scheduleAtFixedRate(new SendRun(digitalDevice), 1000, 1000, TimeUnit.MILLISECONDS);
        es.scheduleAtFixedRate(new SendRun(analogDevice), 1000, 1500, TimeUnit.MILLISECONDS);
        //System.out.println(new File("./").getAbsoluteFile());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                es.shutdown();
                System.out.println("shutdown!!!");
            }
        });

    }

    private static void  randomSetValues(AbstracMonitorDevice device) {
        for (int i = 0; i < device.portNum(); i++) {
            if (device.mtype().equals("SWITCH")) {
                device.setValue(i, new BooleanDeviceValue(ran.nextBoolean()));
            } else if (device.mtype().equals("DIGITAL")) {
                device.setValue(i, new IntegerDeviceValue(ran.nextInt(100), "cnt"));
                i += 1;
            } else {
                device.setValue(i, new DoubleDeviceValue(ran.nextDouble(), "mv"));
            }
        }
    }
}
