package devicegate.actor.message;

import devicegate.conf.JsonField;
import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-6-19.
 */
public class CtrlMessage extends Msg{
    public CtrlMessage() {
        super(TYPE.CTRL);
    }

    public CtrlMessage(JSONObject jo) {
        super(TYPE.CTRL, jo);
    }
}
