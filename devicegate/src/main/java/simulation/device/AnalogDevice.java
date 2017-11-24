package simulation.device;

import simulation.DeviceValue.DoubleDeviceValue;
import simulation.view.DeviceInstance;

/**
 * Created by xiaoke on 17-5-15.
 */
public class AnalogDevice extends DeviceInstance<DoubleDeviceValue> {

    public AnalogDevice(String app, String id, String type, String desc, String company, Location location, long dtimestamp, int portNum) {
        super(app, id, type, desc, company, location, dtimestamp, portNum);
    }

    public AnalogDevice(String app, String id, int portNum) {
        this(app, id, null, null, null, null, System.currentTimeMillis(), portNum);
    }

    public AnalogDevice(String app, String id) {
        this(app, id, 1);
    }

    public String mtype() {
        return TYPE.ANALOG.name();
    }
}
