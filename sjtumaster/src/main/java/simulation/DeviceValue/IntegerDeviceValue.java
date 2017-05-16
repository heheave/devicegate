package simulation.DeviceValue;

/**
 * Created by xiaoke on 17-5-15.
 */
public class IntegerDeviceValue extends AbstractDeviceValue<Integer> {


    public IntegerDeviceValue(boolean isValid, Integer value, String unit) {
        super(isValid, value, unit);
    }

    public IntegerDeviceValue(Integer value, String unit) {
        this(true, value, unit);
    }

    public IntegerDeviceValue(Integer value) {
        this(value, null);
    }
}
