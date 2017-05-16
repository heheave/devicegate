package devicegate.actor.message;

/**
 * Created by xiaoke on 17-5-16.
 */
public class RmIdMessage extends Message {

    public RmIdMessage() {
        super(TYPE.RMID);
    }

    public void setId(String id) {
        data.put("id", id);
    }

    public String getId() {
        if(data.containsKey("id")) {
            return data.getString("id");
        } else {
            return null;
        }
    }

}
