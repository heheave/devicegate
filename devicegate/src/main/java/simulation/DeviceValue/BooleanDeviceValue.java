package simulation.DeviceValue;

/**
 * Created by xiaoke on 17-5-15.
 */
public class BooleanDeviceValue extends AbstractDeviceValue<Integer> {


    public BooleanDeviceValue(boolean isValid, Boolean value) {
        super(isValid, value ? 1 : 0, null);
    }

    public BooleanDeviceValue(boolean value) {
        this(true, value);
    }

    public BooleanDeviceValue() {
        this(false);
    }
}
