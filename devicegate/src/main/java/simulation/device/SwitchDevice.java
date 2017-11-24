package simulation.device;

import simulation.DeviceValue.BooleanDeviceValue;
import simulation.view.DeviceInstance;

/**
 * Created by xiaoke on 17-5-15.
 */
public class SwitchDevice extends DeviceInstance<BooleanDeviceValue> {

    public SwitchDevice(String app, String id, String type, String desc, String company, Location location, long dtimestamp, int portNum) {
        super(app, id, type, desc, company, location, dtimestamp, portNum);
    }

    public SwitchDevice(String app, String id, int portNum) {
        this(app, id, null, null, null, null, System.currentTimeMillis(), portNum);
    }

    public SwitchDevice(String app, String id) {
        this(app, id, 1);
    }

    public String mtype() {
        return TYPE.SWITCH.name();
    }
}
