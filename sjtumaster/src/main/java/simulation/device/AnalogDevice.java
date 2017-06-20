package simulation.device;

import simulation.DeviceValue.DoubleDeviceValue;
import simulation.view.DeviceInstance;

/**
 * Created by xiaoke on 17-5-15.
 */
public class AnalogDevice extends DeviceInstance<DoubleDeviceValue> {

    public AnalogDevice(String id, String type, String desc, String company, Location location, long dtimestamp, int portNum) {
        super(id, type, desc, company, location, dtimestamp, portNum);
    }

    public AnalogDevice(String id, int portNum) {
        this(id, null, null, null, null, System.currentTimeMillis(), portNum);
    }

    public AnalogDevice(String id) {
        this(id, 1);
    }

    public String mtype() {
        return TYPE.ANALOG.name();
    }
}
