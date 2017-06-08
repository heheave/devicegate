package simulation;

import devicegate.conf.JsonField;
import devicegate.conf.V;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.net.SocketServer;
import simulation.DeviceValue.*;
import simulation.device.AbstracMonitorDevice;
import simulation.device.Device;
import simulation.device.DeviceFactory;

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

        private final AbstracMonitorDevice<AbstractDeviceValue<?>> device;

        private Socket s;

        private volatile boolean isCnt;

        public SendRun(Device.TYPE type, int portNum) {
            device = DeviceFactory.getMonitorDevice(type, portNum);
            this.s = new Socket();
            try {
                s.connect(new InetSocketAddress("192.168.1.110", 10000));
                s.setKeepAlive(true);
                isCnt = cnt();
            } catch (IOException e) {
                try {
                    s.close();
                    s = null;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        public void run() {
            if (isCnt) {
                try {
                    monitorOnce(device);
                    log.info(device.toJson().toString());
                    sendDataToRemote(device.toJson().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                log.warn("Not connected");
            }
        }

        private boolean cnt() {
            try {
                JSONObject jo = new JSONObject();
                jo.put(JsonField.DeviceValue.CNT, true);
                jo.put(JsonField.DeviceValue.ID, device.id());
                jo.put(JsonField.DeviceValue.MTYPE, device.mtype());
                jo.put(JsonField.DeviceValue.DESC, "pm2.5");
                String str = jo.toString();
                DataOutputStream dou = new DataOutputStream(s.getOutputStream());
                byte[] bytes = str.getBytes();
                dou.writeInt(bytes.length);
                dou.write(bytes, 0, bytes.length);
                dou.flush();
                DataInputStream dis = new DataInputStream(s.getInputStream());
                int retSize = dis.readInt();
                byte[] bytes1 = new byte[retSize];
                dis.readFully(bytes1, 0, retSize);
                System.out.println(new String(bytes1));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    if (s != null) {
                        s.close();
                    }
                } catch (IOException ioe) {
                    // ignore
                }
            }
            return false;
        }

        private void sendDataToRemote(String str) {
//            if (s == null || s.isClosed()) {
//                throw new RuntimeException("Socket error");
//            }
            try {
                DataOutputStream dou = new DataOutputStream(s.getOutputStream());
                byte[] bytes = str.getBytes();
                dou.writeInt(bytes.length);
                dou.write(bytes, 0, bytes.length);
                dou.flush();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    if (s != null) {
                        s.close();
                    }
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure(V.LOG_PATH);
        log.info("starting...");
        final ScheduledExecutorService es = Executors.newScheduledThreadPool(3);
        es.scheduleAtFixedRate(new SendRun(Device.TYPE.SWITCH, 1), 1000, 15000, TimeUnit.MILLISECONDS);
        es.scheduleAtFixedRate(new SendRun(Device.TYPE.DIGITAL, 2), 1000, 15000, TimeUnit.MILLISECONDS);
        es.scheduleAtFixedRate(new SendRun(Device.TYPE.ANALOG, 4), 1000, 15000, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                es.shutdown();
                log.info("shutdown!!!");
            }
        });

    }

    private static void  monitorOnce(AbstracMonitorDevice<AbstractDeviceValue<?>> device) {
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
