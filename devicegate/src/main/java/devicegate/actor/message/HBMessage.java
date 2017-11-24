package devicegate.actor.message;

import devicegate.conf.JsonField;

/**
 * Created by xiaoke on 17-5-17.
 */
public class HBMessage extends Msg{

    public HBMessage() {
        super(TYPE.HB);
    }

    public void setCpuUsage(float cu) {
        data.put(JsonField.MSG.CU, cu);
    }

    public double getCpuUsage() {
        if(data.containsKey(JsonField.MSG.CU)) {
            return data.getDouble(JsonField.MSG.CU);
        } else {
            return 0.0;
        }
    }

    public void setMemUsage(float cu) {
        data.put(JsonField.MSG.MU, cu);
    }

    public double getMemUsage() {
        if(data.containsKey(JsonField.MSG.MU)) {
            return data.getDouble(JsonField.MSG.MU);
        } else {
            return 0.0;
        }
    }

    public void setIoUsage(float cu) {
        data.put(JsonField.MSG.IU, cu);
    }

    public double getIoUsage() {
        if(data.containsKey(JsonField.MSG.IU)) {
            return data.getDouble(JsonField.MSG.IU);
        } else {
            return 0.0;
        }
    }

    public void setNetUsage(float cu) {
        data.put(JsonField.MSG.NU, cu);
    }

    public double getNetUsage() {
        if(data.containsKey(JsonField.MSG.NU)) {
            return data.getDouble(JsonField.MSG.NU);
        } else {
            return 0.0;
        }
    }
}
