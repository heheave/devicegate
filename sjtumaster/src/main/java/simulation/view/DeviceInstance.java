package simulation.view;

import devicegate.conf.JsonField;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import simulation.DeviceValue.*;
import simulation.device.AbstracMonitorDevice;
import simulation.device.Location;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;

/**
 * Created by xiaoke on 17-6-19.
 */
abstract public class DeviceInstance<T extends DeviceValue<?>> extends AbstracMonitorDevice<T>{

    private static final Logger log = Logger.getLogger(DeviceInstance.class);

    private Socket s;

    private volatile boolean isActive;

    private volatile boolean isCnt;

    public DeviceInstance(String id, String type, String desc, String company, Location location, long dtimestamp, int portNum) {
        super(id, type, desc, company, location, dtimestamp, portNum);
        this.s = new Socket();
        try {
            s.connect(new InetSocketAddress("192.168.1.110", 10000));
            s.setKeepAlive(true);
            //isCnt = cnt();
        } catch (IOException e) {
            try {
                s.close();
                s = null;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void start() {
        isActive = true;
        Thread t = new Thread() {
            public void run() {
                receiverDataFromRemote();
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private boolean cnt() {
        try {
            JSONObject jo = new JSONObject();
            jo.put(JsonField.DeviceValue.CNT, true);
            jo.put(JsonField.DeviceValue.ID, id());
            jo.put(JsonField.DeviceValue.MTYPE, mtype());
            jo.put(JsonField.DeviceValue.DESC, "pm2.5");
            String str = jo.toString();
            DataOutputStream dou = new DataOutputStream(s.getOutputStream());
            byte[] bytes = str.getBytes();
            dou.writeInt(bytes.length);
            dou.write(bytes, 0, bytes.length);
            dou.flush();
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

    private void receiverDataFromRemote(){
        try {
            DataInputStream dis = new DataInputStream(s.getInputStream());
            while (isActive) {
                int len = dis.readInt();
                if (len > 0) {
                    byte[] bytes = new byte[len];
                    dis.readFully(bytes, 0, len);
                    log.info("Receive data: " + new String(bytes));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    public void simulateOnce() {
        monitorOnce();
        sendDataToRemote(this.toJson().toString());
    }

    private void  monitorOnce() {
        Random ran = new Random();
        for (int i = 0; i < portNum(); i++) {
            if (mtype().equals("SWITCH")) {
                ((DeviceInstance<BooleanDeviceValue>)this)
                        .setValue(i, new BooleanDeviceValue(ran.nextBoolean()));
            } else if (mtype().equals("DIGITL")) {
                ((DeviceInstance<IntegerDeviceValue>)this)
                        .setValue(i, new IntegerDeviceValue(ran.nextInt(100), "cnt"));
                i += 1;
            } else {
                ((DeviceInstance<DoubleDeviceValue>)this).
                        setValue(i, new DoubleDeviceValue(ran.nextDouble(), "mv"));
            }
        }
    }

    public void sendDataToRemote(String str) {
        if (!isCnt) {
            synchronized (this) {
                if (!isCnt) {
                    isCnt = cnt();
                }
            }
        }
        try {
            DataOutputStream dou = new DataOutputStream(s.getOutputStream());
            byte[] bytes = str.getBytes();
            dou.writeInt(bytes.length);
            dou.write(bytes, 0, bytes.length);
            dou.flush();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    public void close() {
        isActive = false;
        try {
            if (s != null) {
                s.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
}
