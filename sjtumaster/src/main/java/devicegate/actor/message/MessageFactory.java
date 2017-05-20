package devicegate.actor.message;

/**
 * Created by xiaoke on 17-5-17.
 */
public class MessageFactory {

    public static Msg getMessage(Msg.TYPE type) {
        Msg ret = null;
        switch (type) {
            case ACK:
                ret = new AckMessage();
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
            default:
        }
        return ret;
    }

}
