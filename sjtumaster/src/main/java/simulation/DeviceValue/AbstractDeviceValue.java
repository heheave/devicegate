package simulation.DeviceValue;

import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-5-15.
 */

// One type device can only detect on type value
public class AbstractDeviceValue<T> implements DeviceValue<T> {

    protected final boolean valid;

    protected final T value;

    protected final String unit;

    // Do not set timestamp in device end by default
    public AbstractDeviceValue(boolean valid, T value, String unit) {
        this.valid = valid;
        this.value = value;
        this.unit = unit;
    }

//    public AbstractDeviceValue(T value, String unit) {
//        this(true, value, unit);
//    }
//
//    public AbstractDeviceValue(T value) {
//        this(value, null);
//    }

    public boolean isValid() {
        return valid;
    }

    public T getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }
}
