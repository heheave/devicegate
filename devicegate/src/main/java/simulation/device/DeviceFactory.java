package simulation.device;

import simulation.view.DeviceInstance;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xiaoke on 17-5-15.
 */
public class DeviceFactory {

    //public static final String SWITCH_MAGIC_STRING = "SWITCH";
    //public static final String DIGITAL_MAGIC_STRING = "DIGITL";
    //public static final String ANALOG_MAGIC_STRING = "ANALOG";


    private static final AtomicInteger ai = new AtomicInteger(9);

    private static final Set<String> deviceIds = new HashSet<String>();

    public static DeviceInstance getMonitorDevice(Device.TYPE deviceType, String app, String dmark, int portNum) {
        DeviceInstance device = null;
        String actualId = getLegalId(app, dmark);
        if (actualId != null) {
            switch (deviceType) {
                case SWITCH:
                    device = new SwitchDevice(app, actualId, portNum);
                    break;
                case DIGITL:
                    device = new DigitalDevice(app, actualId, portNum);
                    break;
                default:
                    device = new AnalogDevice(app, actualId, portNum);
            }
            return device;
        } else {
            return null;
        }
    }

    private static String getLegalId(String app, String dmark) {
        //String genId = UUID.randomUUID().toString().substring(0, 7).toUpperCase();
        String actualId = String.format("%s_%s", app, dmark);
        synchronized (deviceIds) {
            if (deviceIds.contains(actualId)) {
                return null;
            } else {
                deviceIds.add(actualId);
                return dmark;
            }
        }
    }
}
