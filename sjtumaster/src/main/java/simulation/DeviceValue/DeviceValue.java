package simulation.DeviceValue;

import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-5-15.
 */
public interface DeviceValue<T> {

    boolean isValid();

    T getValue();

    String getUnit();
}
