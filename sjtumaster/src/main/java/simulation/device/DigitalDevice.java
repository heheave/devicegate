package simulation.device;

import simulation.DeviceValue.IntegerDeviceValue;
import simulation.view.DeviceInstance;

/**
 * Created by xiaoke on 17-5-15.
 */
public class DigitalDevice extends DeviceInstance<IntegerDeviceValue> {

    public DigitalDevice(String id, String type, String desc, String company, Location location, long dtimestamp, int portNum) {
        super(id, type, desc, company, location, dtimestamp, portNum);
    }

    public DigitalDevice(String id, int portNum) {
        this(id, null, null, null, null, System.currentTimeMillis(), portNum);
    }

    public DigitalDevice(String id) {
        this(id, 1);
    }

    public String mtype() {
        return TYPE.DIGITL.name();
    }
}
