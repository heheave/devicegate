package devicegate.actor.message;

import devicegate.conf.JsonField;

/**
 * Created by xiaoke on 17-5-16.
 */
public class AddIdMessage extends Msg {

    public AddIdMessage() {
        super(TYPE.ADDID);
    }

    public void setProtocol(String protocol) {
        data.put(JsonField.MSG.PTC, protocol);
    }

    public String getProtocol() {
        if(data.containsKey(JsonField.MSG.PTC)) {
            return data.getString(JsonField.MSG.PTC);
        } else {
            return null;
        }
    }
}
