package devicegate.actor.message;

import net.sf.json.JSONObject;

/**
 * Created by xiaoke on 17-5-17.
 */
public class MessageFactory {

    public static Msg getMessage(Msg.TYPE type) {
        return getMessage(type, null);
    }

    public static Msg getMessage(Msg.TYPE type, JSONObject jo) {
        Msg ret = null;
        switch (type) {
            case ACK:
                ret = jo != null ? new AckMessage(jo) : new AckMessage();
                break;
            case ADDID:
                ret = new AddIdMessage();
                break;
            case RMID:
                ret = new RmIdMessage();
                break;
            case STASLV:
                ret = new StartSlvMessage();
                break;
            case STPSLV:
                ret = new StopSlvMessage();
                break;
            case HB:
                ret = new HBMessage();
                break;
            case TELLME:
                ret = new TellMeMessage();
                break;
            case CTRL:
                ret = jo != null ? new CtrlMessage(jo) : new CtrlMessage();
                break;
            case DINFO:
                ret = jo != null ? new DinfoMessage(jo) : new DinfoMessage();
                break;
            default:
        }
        return ret;
    }

}
