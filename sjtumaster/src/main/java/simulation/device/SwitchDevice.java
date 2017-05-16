package simulation.device;

import simulation.DeviceValue.BooleanDeviceValue;

/**
 * Created by xiaoke on 17-5-15.
 */
public class SwitchDevice extends AbstracMonitorDevice<BooleanDeviceValue> {

    public SwitchDevice(String id, String type, String desc, String company, Location location, long dtimestamp, int portNum) {
        super(id, type, desc, company, location, dtimestamp, portNum);
    }

    public SwitchDevice(String id, int portNum) {
        this(id, null, null, null, null, System.currentTimeMillis(), portNum);
    }

    public SwitchDevice(String id) {
        this(id, 1);
    }

    public String mtype() {
        return TYPE.SWITCH.name();
    }
}
