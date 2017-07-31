package simulation.DeviceValue;

/**
 * Created by xiaoke on 17-5-15.
 */
public class DoubleDeviceValue extends AbstractDeviceValue<Double> {


    public DoubleDeviceValue(boolean isValid, Double value, String unit) {
        super(isValid, value, unit);
    }

    public DoubleDeviceValue(Double value, String unit) {
        this(true, value, unit);
    }

    public DoubleDeviceValue(Double value) {this(value, null);}

}
