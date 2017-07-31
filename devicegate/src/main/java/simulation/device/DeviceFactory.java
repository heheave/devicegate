package simulation.device;

import simulation.view.DeviceInstance;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xiaoke on 17-5-15.
 */
public class DeviceFactory {

    public static final String SWITCH_MAGIC_STRING = "SWITCH";
    public static final String DIGITAL_MAGIC_STRING = "DIGITL";
    public static final String ANALOG_MAGIC_STRING = "ANALOG";


    private static final AtomicInteger ai = new AtomicInteger(9);

    private static final Set<String> deviceIds = new HashSet<String>();

    public static DeviceInstance getMonitorDevice(Device.TYPE deviceType, int portNum) {
        DeviceInstance device = null;
        String actualId = getLegalId(deviceType);
        switch (deviceType) {
            case SWITCH:
                device = new SwitchDevice(actualId, portNum);
                break;
            case DIGITL:
                device = new DigitalDevice(actualId, portNum);
                break;
            default:
                device = new AnalogDevice(actualId, portNum);
        }
        return device;
    }

    private static String getLegalId(Device.TYPE deviceType) {
        //String genId = UUID.randomUUID().toString().substring(0, 7).toUpperCase();
        int i = ai.getAndIncrement();
        String actualId;
        String tail = String.format("ABC%03d", i);
        switch (deviceType) {
            case SWITCH:
                actualId = SWITCH_MAGIC_STRING + "-" + tail;
                break;
            case DIGITL:
                actualId = DIGITAL_MAGIC_STRING + "-" + tail;
                break;
            default:
                actualId = ANALOG_MAGIC_STRING + "-" + tail;
        }
        synchronized (deviceIds) {
            if (deviceIds.contains(actualId)) {
                return null;
            } else {
                deviceIds.add(actualId);
                return actualId;
            }
        }
    }
}
