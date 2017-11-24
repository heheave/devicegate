package devicegate.actor.message;

import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-11-13.
 */
public class DinfoMessage extends Msg{

    public DinfoMessage(JSONObject jo) {
        super(TYPE.DINFO, jo);
    }

    public DinfoMessage() {
        super(TYPE.DINFO);
    }

}
