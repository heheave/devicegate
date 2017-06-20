package devicegate.actor.message;

import devicegate.conf.JsonField;
import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-5-16.
 */
public class AckMessage extends Msg {
    public AckMessage() {
        super(TYPE.ACK);
    }

    public AckMessage(JSONObject jo) {
        super(TYPE.ACK, jo);
    }

    public void setAckInfo(String info) {
        if (info != null) {
            data.put(JsonField.MSG.ACKINFO, info);
        }
    }

    public String getAckInfo() {
        if (data.containsKey(JsonField.MSG.ACKINFO)) {
            return  data.getString(JsonField.MSG.ACKINFO);
        } else {
            return null;
        }
    }
}
