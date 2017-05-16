package simulation.DeviceValue;

/**
 * Created by xiaoke on 17-5-15.
 */
public class BooleanDeviceValue extends AbstractDeviceValue<Boolean> {


    public BooleanDeviceValue(boolean isValid, Boolean value) {
        super(isValid, value, null);
    }

    public BooleanDeviceValue(boolean value) {
        this(true, value);
    }

    public BooleanDeviceValue() {
        this(false);
    }
}
