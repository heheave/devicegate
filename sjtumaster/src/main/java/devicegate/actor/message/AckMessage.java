package devicegate.actor.message;

/**
 * Created by xiaoke on 17-5-16.
 */
public class AckMessage extends Msg {
    public AckMessage() {
        super(TYPE.ACK, null);
    }
}
