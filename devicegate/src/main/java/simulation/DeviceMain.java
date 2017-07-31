package simulation;

import devicegate.conf.JsonField;
import devicegate.conf.V;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import simulation.DeviceValue.*;
import simulation.device.AbstracMonitorDevice;
import simulation.device.Device;
import simulation.device.DeviceFactory;
import simulation.view.DeviceFrame;
import simulation.view.DeviceInstance;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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

        public SendRun(Device.TYPE type, int portNum) {
            device = DeviceFactory.getMonitorDevice(type, portNum);
            device.start();
        }

        public void run() {
            device.simulateOnce();
        }

        public void stop() {
            device.close();
        }

    }

    public static void main(String[] args) {
        PropertyConfigurator.configure(V.LOG_PATH);
        log.info("starting...");
        final ScheduledExecutorService es = Executors.newScheduledThreadPool(3);
        final SendRun switchDevice = new SendRun(Device.TYPE.SWITCH, 1);
        final SendRun digitalDevice = new SendRun(Device.TYPE.DIGITL, 2);
        final SendRun analogDevice = new SendRun(Device.TYPE.ANALOG, 4);
        es.scheduleAtFixedRate(switchDevice, 1000, 15000, TimeUnit.MILLISECONDS);
        es.scheduleAtFixedRate(digitalDevice, 1000, 15000, TimeUnit.MILLISECONDS);
        es.scheduleAtFixedRate(analogDevice, 1000, 15000, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                es.shutdown();
                switchDevice.stop();
                digitalDevice.stop();
                analogDevice.stop();
                log.info("shutdown!!!");
            }
        });
        //JFrame jFrame = new DeviceFrame("ABC001");

    }
}
