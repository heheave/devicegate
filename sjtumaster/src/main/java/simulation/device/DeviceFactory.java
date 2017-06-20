package simulation.device;

import simulation.view.DeviceInstance;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xiaoke on 17-5-15.
 */
public class DeviceFactory {

    public static final String SWITCH_MAGIC_STRING = "SWITCH";
    public static final String DIGITAL_MAGIC_STRING = "DIGITL";
    public static final String ANALOG_MAGIC_STRING = "ANALOG";

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
        String actualId;
        switch (deviceType) {
            case SWITCH:
                actualId = SWITCH_MAGIC_STRING + "-" + "ABC001";
                break;
            case DIGITL:
                actualId = DIGITAL_MAGIC_STRING + "-" + "ABC002";
                break;
            default:
                actualId = ANALOG_MAGIC_STRING + "-" + "ABC003";
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
