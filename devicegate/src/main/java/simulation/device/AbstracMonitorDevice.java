package simulation.device;

import devicegate.conf.JsonField;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import simulation.DeviceValue.DeviceValue;

/**
 * Created by xiaoke on 17-5-15.
 */
abstract public class AbstracMonitorDevice<T extends DeviceValue<?>> implements Device{

    protected final String id;

    protected String dtype;

    protected String desc;

    protected String company;

    protected Location location;

    protected  long dtimestamp;

    protected int portNum;

    protected DeviceValue<?>[] values;

    public AbstracMonitorDevice(String id, String type, String desc, String company, Location location, long dtimestamp, int portNum) {
        this.id = id;
        this.dtype = type;
        this.desc = desc;
        this.company = company;
        this.location = location;
        this.dtimestamp = dtimestamp;
        this.portNum = portNum;
        this.values = new DeviceValue<?>[portNum];
    }

    public String id() {
        return id;
    }

    public String dtype() {
        return dtype;
    }

    public String desc() {
        return desc;
    }

    public String company() {
        return company;
    }

    public Location location() {
        return location;
    }

    public long dtimestamp() {
        return dtimestamp;
    }

    public int portNum() {
        return portNum;
    }

    public boolean setValue(T deviceValue) {
        return setValue(portNum - 1, deviceValue);
    }

    public boolean setValue(int idx, T deviceValue) {
        if (idx < 0 || idx >= portNum) {
            return false;
        }
        values[idx] = deviceValue;
        dtimestamp = System.currentTimeMillis();
        return true;
    }

    public JSONObject toJson() {
        JSONObject jo = new JSONObject();
        jo.put(JsonField.DeviceValue.APP, "app1");
        jo.put(JsonField.DeviceValue.ID, id);
        jo.put(JsonField.DeviceValue.DTYPE, dtype);
        jo.put(JsonField.DeviceValue.DESC, desc);
        jo.put(JsonField.DeviceValue.COM, company);
        jo.put(JsonField.DeviceValue.LOC, location == null ? null : JSONObject.fromObject(location));
        jo.put(JsonField.DeviceValue.PORTNUM, portNum);
        jo.put(JsonField.DeviceValue.DTIMESTAMP, dtimestamp);
        jo.put(JsonField.DeviceValue.MTYPE, mtype());
        JSONArray ja = new JSONArray();
        for (int i = 0; i < portNum; i++) {
            if (values[i] == null) {
                ja.add(i, new JSONObject());
            } else {
                ja.add(i, JSONObject.fromObject(values[i]));
            }
        }
        jo.put(JsonField.DeviceValue.VALUES, ja);
        return jo;
    }
}
